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
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._

import actors.{ Client, HttpGate }


object Main extends App {
  private val config = ConfigFactory.load
  private val host = config.as[String]("http.hostname")
  private val port = config.as[Int]("http.port")
  private val servers = config.as[List[String]]("hippo.servers")

  implicit val system = ActorSystem("hippo-http")
  val client = system.actorOf(
    Props(new Client(servers)),
    name = "client"
  )
  val http = system.actorOf(
    Props(new HttpGate(client)),
    name = "http"
  )

  IO(Http) ! Http.Bind(http, interface = host, port = port)
}