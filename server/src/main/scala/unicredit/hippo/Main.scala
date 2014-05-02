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

import akka.actor.{ ActorSystem, Props }
import akka.contrib.pattern.ClusterReceptionistExtension
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._

import actors.{ Retriever, Frontend }


object Main extends App {
  private val config = ConfigFactory.load
  private val home = config.as[String]("storage.home")

  implicit val system = ActorSystem("hippo")
  val retriever = system.actorOf(
    Props(new Retriever(home)),
    name = "retriever"
  )
  val frontend = system.actorOf(
    Props(new Frontend(retriever)),
    name = "frontend"
  )

  ClusterReceptionistExtension(system).registerService(frontend)
}