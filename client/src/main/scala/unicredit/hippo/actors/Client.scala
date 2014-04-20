package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.contrib.pattern.ClusterClient
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import common.shard
import messages.{ RefreshNodes, GetSiblings, Siblings }


class Client(contacts: Seq[String]) extends Actor with ActorLogging {
  private val initialContacts = contacts map { url ⇒
      context.actorSelection(s"$url/user/receptionist")
    } toSet
  private val tms = 15 // config getLong "request.timeout-in-s"
  implicit val timeout = Timeout(tms seconds)

  val clusterClient = context.actorOf(ClusterClient.props(initialContacts))
  var nodes = Map.empty[String, ActorRef]

  import context.dispatcher
  self ! RefreshNodes

  def receive = {
    case RefreshNodes ⇒
      clusterClient ! ClusterClient.Send("/user/frontend", GetSiblings, localAffinity = true)
      context.system.scheduler.scheduleOnce(1 minute) { self ! RefreshNodes }
    case Siblings(siblings) ⇒
      nodes = siblings
    case s ⇒
      val id = shard(s.toString, nodes.keySet.toList)
      val destination = nodes(id)
      destination ? s pipeTo sender
  }
}