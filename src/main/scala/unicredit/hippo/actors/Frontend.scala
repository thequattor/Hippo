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

import messages.{ Request, Retrieve, IdentifyTo, MyIdIs }
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
    // Actual request messages
    case Request(table, keys, columns) ⇒
      val results = keys map { key =>
        val message = Retrieve(table, List(key), columns)
        val remotes = shard.indicesFor(key, siblings.keySet.toSeq, replicas).toStream
        // Changed this with a proper implementation of streams
        // that does not force its first element
        val lazyRequests = remotes map { id => siblings(id) ? message }

        if (remotes contains id) { self ? message }
        else firstOf(lazyRequests)
      }
    case m: Retrieve ⇒
      retriever ? m pipeTo sender
  }


  def firstOf[A](futures: => Stream[Future[A]]): Future[A] = futures match {
    case Stream() => Future.failed(new Exception("No future completed"))
    // Unfortunately, Future#fallbackTo evaluates its argument
    // eagerly, and this would force us to spawn more requests
    // than needed.
    case h #:: t => h recoverWith { case _: Throwable => firstOf(t) }
  }

  def siblingActor(member: Member) = {
    val path = RootActorPath(member.address) / "user" / "frontend"

    context.actorSelection(path)
  }
}
