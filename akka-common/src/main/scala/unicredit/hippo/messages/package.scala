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

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorRef


package object messages {
  case class IdentifyTo(who: ActorRef)
  case class MyIdIs(who: ActorRef, id: String)
  case class Request(
    table: String,
    keys: List[String],
    columns: List[String]
  )
  case class Retrieve(
    table: String,
    keys: List[String],
    columns: List[String]
  )
  case class Result(content: Map[String, Map[String, String]])
  case class Switch(version: String)
  case object GetSiblings
  case class Siblings(nodes: Map[String, ActorRef])
  case object GetInfo
  case class Info(
    nodes: Map[String, ActorRef],
    timeout: FiniteDuration,
    cacheSize: Int,
    replicas: Int
  )
  case object RefreshNodes
  case object AreYouReady
  case class ReadyState(state: Future[Boolean])
}