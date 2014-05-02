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
package retriever

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import actors.Downloader
import messages.Download


object Main extends App {
  implicit val timeout = Timeout(10 minutes)
  val system = ActorSystem("hippo-retriever")
  val downloader = system.actorOf(
    Props[Downloader],
    name = "downloader"
  )
  import system.dispatcher

  Parser.parse(args, { config ⇒
    val baseDir = s"${ config.source }/${ config.table }/${ config.id }/shards"
    val shards = IO.listFiles(baseDir)

    val futures = for {
      shard <- shards
      message = Download(baseDir, config.target, shard)
    } yield downloader ? message

    Future.sequence(futures) onComplete { _ ⇒
      system.shutdown
    }
  })
}