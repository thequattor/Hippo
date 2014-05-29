/*  Copyright 2014 UniCredit S.p.A.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }
import scala.math.pow

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
  private var serverDuration = 10 seconds
  implicit var timeout = Timeout(serverDuration)
  private val ready = Promise[Boolean]()

  val clusterClient = context.actorOf(ClusterClient.props(initialContacts))
  var nodes = Map.empty[String, ActorRef]

  import context.dispatcher
  self ! RefreshNodes

  def receive = {
    case RefreshNodes ⇒
      clusterClient ! ClusterClient.Send("/user/frontend", GetInfo, localAffinity = true)
      context.system.scheduler.scheduleOnce(30 seconds) { self ! RefreshNodes }
    case Info(siblings, duration, _, _) ⇒
      nodes = siblings
      serverDuration = duration
      log.info("{} - {}", siblings, duration)
      // See `generateTimeouts` for how this is chosen
      timeout = duration * 22 div 10
      if (! ready.isCompleted) { ready.success(true) }
    case AreYouReady ⇒
      sender ! ReadyState(ready.future)
    case GetSiblings ⇒
      sender ! nodes
    case m @ (_: Request | _: SwitchAll | GetInfo) ⇒
      val ids = shards(m.toString, nodes.keySet.toList, 2)
      val actors = ids map nodes
      val timeouts = generateTimeouts(2)
      val result = actors(0).?(m)(timeouts(0)) orElse {
        // No reply from the first destination, we ask
        // somewhere else. But in the meantime, better
        // refresh our node list, since someone went down.
        log.info(s"Lost contact with ${ ids.head }, refreshing node list")
        self ! RefreshNodes
        actors(1).?(m)(timeouts(1))
      }

      result pipeTo sender
  }

  // The timeouts for the requests are chosen as follows.
  // The first one is slightly higher than the server timeout.
  // Each successive timeout is divided by a factor of 2.
  // This guarantees that the first request has time to
  // complete or fail by server timeout, but bounds the total
  // time we spend contacting server at 2.2 times the server
  // timeout. This is the value we set the client timeout to.
  def generateTimeouts(n: Int) = 1 to n map { i ⇒
      val baseDuration = serverDuration * 11 div 10

      Timeout(baseDuration div pow(2, i).toInt)
    }
}