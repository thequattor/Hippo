package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor._
import akka.cluster.{ Cluster, Member, MemberStatus }
import akka.cluster.ClusterEvent._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scalaz.Scalaz._

import messages._
import sharding.RemoteShard


class Frontend(retriever: ActorRef) extends Actor with ActorLogging {
  private val config = ConfigFactory.load
  private val id = config getString "storage.local-id"
  private val tms = config getLong "request.timeout-in-s"
  private val replicas = config getInt "storage.replicas"
  implicit val timeout = Timeout(tms seconds)
  import context.dispatcher

  val cluster = Cluster(context.system)
  var siblings = Map(id -> self)
  val shard = new RemoteShard

  override def preStart() = cluster.subscribe(self, classOf[MemberUp])
  override def postStop() = cluster.unsubscribe(self)

  val emptyFuture = Future.failed(new Exception("No future completed"))

  def receive = {
    // Membership messages
    case MemberUp(member) ⇒
      log.info(s"Recognized new member $member")
      val frontend = siblingActor(member)

      frontend ! IdentifyTo(self)
      frontend ! MyIdIs(self, id)
    case MyIdIs(actor, id) ⇒
      siblings += (id -> actor)
      context watch sender
    case Terminated(actor) ⇒
      log.info(s"Lost contact with actor $actor")
      siblings = siblings filterNot { case (_, a) => a == actor }
      log.info(s"Siblings are now ${ siblings.keySet }")
    // Actual request messages
    case Request(table, keys, columns) ⇒
      // We make a separate request for each key. This is less
      // efficient than grouping the keys by shard and issuing
      // a single request to each server, but it has the advantage
      // that it is very easy to deal with failures by looking for
      // the key in the next shard. Implementation may change in the
      // future if this becomes a performance issue.
      val results = keys map { key =>
        val message = Retrieve(table, List(key), columns)
        val remotes = shard.indicesFor(key, siblings.keySet.toSeq, replicas).toStream
        // Change this with a proper implementation of streams
        // that does not force its first element.
        // Right now we just insert a dummy element at the head
        // of the stream to avoid firing a remore request if
        // it is not needed.
        val lazyRequests = emptyFuture #:: (remotes map { id =>
          log.info(s"Sending request to $id for key $key")
          siblings(id) ? message
        })
        val future = if (remotes contains id) { self ? message } else firstOf(lazyRequests)

        future.mapTo[Result]
      }

      Future.sequence(results) map accumulate pipeTo sender
    case m: Retrieve ⇒
      retriever ? m pipeTo sender
  }

  def firstOf[A](futures: => Stream[Future[A]]): Future[A] = futures match {
    case Stream() => emptyFuture
    // One may think to match h #:: t, but this would eagerly
    // evaluate the head of t, firing one more request than
    // needed.
    case stream => stream.head recoverWith { case _: Throwable => firstOf(stream.tail) }
    // Unfortunately, Future#fallbackTo evaluates its argument
    // eagerly, and this would force us to spawn more requests
    // than needed. Hence the use of Future#recoverWith
  }

  def siblingActor(member: Member) = {
    val path = RootActorPath(member.address) / "user" / "frontend"

    context.actorSelection(path)
  }

  def accumulate(results: Iterable[Result]) =
    results.foldLeft(Result(Map())) { case (Result(m1), Result(m2)) =>
      Result(m1 |+| m2)
    }
}
