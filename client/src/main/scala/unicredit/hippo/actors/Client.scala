package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.{ Actor, ActorLogging }
import akka.contrib.pattern.ClusterClient
import akka.util.Timeout

import common.shards


class Client(contacts: Seq[String]) extends Actor with ActorLogging {
  private val initialContacts = contacts map { url ⇒
      context.actorSelection(s"$url/user/receptionist")
    } toSet
  private val tms = 15 // config getLong "request.timeout-in-s"
  implicit val timeout = Timeout(tms seconds)

  val clusterClient = context.actorOf(ClusterClient.props(initialContacts))

  def receive = {
    case s: String ⇒
      clusterClient ! ClusterClient.Send("/user/frontend", s, localAffinity = false)
  }
}