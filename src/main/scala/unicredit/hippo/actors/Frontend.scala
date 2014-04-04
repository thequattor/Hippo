package unicredit.hippo
package actors

import scala.concurrent.duration._

import akka.actor._
import akka.cluster.{ Cluster, Member, MemberStatus }
import akka.cluster.ClusterEvent._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import messages.{ Request, Retrieve, IdentifyTo, MyIdIs }


class Frontend(retriever: ActorRef) extends Actor with ActorLogging {
  private val config = ConfigFactory.load
  private val id = config getString "storage.local-id"
  private val tms = config getLong "request.timeout-in-s"
  implicit val timeout = Timeout(tms seconds)
  import context.dispatcher

  val cluster = Cluster(context.system)
  var siblings = Map.empty[ActorRef, String]

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
      siblings += (actor -> id)
      context watch sender
    case Terminated(actor) ⇒
      log.info(s"Lost contact with actor $actor")
      siblings = siblings filterNot { case (a, _) => a == actor }
    // Actual request messages
    case Request(table, keys, columns) ⇒

    case m: Retrieve ⇒
      retriever ? m pipeTo sender
  }

  def siblingActor(member: Member) = {
    val path = RootActorPath(member.address) / "user" / "frontend"

    context.actorSelection(path)
  }
}
