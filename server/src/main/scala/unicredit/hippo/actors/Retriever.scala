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
import scala.collection.mutable.{ HashMap ⇒ MMap }

import akka.actor.{ Actor, ActorLogging }
import akka.util.Timeout

import storage.Repository
import messages.{ Retrieve, Result, Switch }


class Retriever(home: String) extends Actor with ActorLogging {
  val repos = MMap.empty[String, Repository]
  var version = "abcd123"

  def receive = {
    case Retrieve(table, keys, columns) ⇒
      val repo = repos.getOrElseUpdate(table,
        new Repository(home, table, version))
      val data = repo.read(keys, columns)

      sender ! Result(data)
    case Switch(v) ⇒
      log.info(s"Switching to version $v")
      version = v
      repos.clear
  }
}