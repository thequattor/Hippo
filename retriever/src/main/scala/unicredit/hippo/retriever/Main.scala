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
import akka.routing.{ SmallestMailboxPool, DefaultResizer }

import actors.Downloader
import messages.Download


object Main extends App {
  implicit val timeout = Timeout(10 minutes)

  Parser.parse(args, { config ⇒
    val system = ActorSystem("hippo-retriever")
    val resizer = DefaultResizer(lowerBound = 2, upperBound = 16)
    val downloader = system.actorOf(
      SmallestMailboxPool(5, Some(resizer)).props(Props[Downloader]),
      name = "downloader"
    )
    import system.dispatcher
    val source = s"${ config.source }/${ config.version }/${ config.table }/${ config.id }/shards"
    val target = s"${ config.target }/${ config.version }/${ config.table }"
    val shards = IO.listFiles(source)
    IO.mkdir(target)

    val futures = for {
      shard <- shards
      message = Download(source, target, shard)
    } yield downloader ? message

    Future.sequence(futures) onComplete { _ ⇒
      system.shutdown
    }
  })
}