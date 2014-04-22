package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }

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
  implicit val timeout = Timeout(5 seconds)
  private val ready = Promise[Boolean]()

  val clusterClient = context.actorOf(ClusterClient.props(initialContacts))
  var nodes = Map.empty[String, ActorRef]

  import context.dispatcher
  self ! RefreshNodes

  def receive = {
    case RefreshNodes ⇒
      clusterClient ! ClusterClient.Send("/user/frontend", GetSiblings, localAffinity = true)
      context.system.scheduler.scheduleOnce(30 seconds) { self ! RefreshNodes }
    case Siblings(siblings) ⇒
      nodes = siblings
      if (! ready.isCompleted) { ready.success(true) }
    case AreYouReady ⇒
      sender ! ReadyState(ready.future)
    case GetSiblings ⇒
      sender ! nodes
    case m: Request ⇒
      val ids = shards(m.toString, nodes.keySet.toList, 2)
      val actors = ids map nodes
      val result = (actors.head ? m) orElse {
        // No reply from the first destination, we ask
        // somewhere else. But in the meantime, better
        // refresh our node list, since someone went down.
        log.info(s"Lost contact with ${ ids.head }, refreshing node list")
        self ! RefreshNodes
        (actors.tail.head ? m)
      }

      result pipeTo sender
  }
}