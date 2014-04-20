package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.contrib.pattern.ClusterClient
import akka.pattern.pipe
import akka.util.Timeout

import common.shards
import pattern.{ fixedAsk, fallback }
import messages._


class Client(contacts: Seq[String]) extends Actor with ActorLogging {
  private val initialContacts = contacts map { url ⇒
      context.actorSelection(s"$url/user/receptionist")
    } toSet
  private val tms = 5 // config getLong "request.timeout-in-s"
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
    case m: Request ⇒
      val ids = shards(m.toString, nodes.keySet.toList, 3)
      val List(d1, d2, d3) = ids map nodes
      val result = (d1 ? m) orElse (d2 ? m) orElse (d3 ? m)

      result pipeTo sender
  }
}