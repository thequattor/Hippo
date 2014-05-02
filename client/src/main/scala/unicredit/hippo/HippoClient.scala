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

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import actors.Client
import messages._


// This client is meant to be used from Scala
// applications that are not based on Akka. It
// starts its own actor system.
class HippoClient(host: String, port: Int) {
  private val contact = s"akka.tcp://hippo@$host:$port"
  private val system = ActorSystem("hippo-client")
  private implicit val timeout = Timeout(5 seconds)
  private val client = system.actorOf(
    Props(new Client(List(contact))),
    name = "client"
  )
  import system.dispatcher

  // Since the client will be used outside of Akka,
  // users may not have a way to wait for the client
  // to be ready (asynchronously). We do the easy
  // thing and just block until the client has
  // established connection to the cluster.
  private val ready = Await.result((client ? AreYouReady).mapTo[ReadyState], 5 seconds)
  Await.ready(ready.state, 5 seconds)

  def query(request: Request, callback: Result ⇒ Unit) =
    (client ? request).mapTo[Result] onSuccess PartialFunction(callback)
  def siblings(callback: Siblings ⇒ Unit) =
    (client ? GetSiblings).mapTo[Siblings] onSuccess PartialFunction(callback)
  def close = system.shutdown
}