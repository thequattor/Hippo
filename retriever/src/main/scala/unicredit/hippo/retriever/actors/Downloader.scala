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
package actors

import akka.actor.{ Actor, ActorLogging }

import messages.Download


class Downloader extends Actor with ActorLogging {
  def receive = {
    case Download(source, target, shard) ⇒
      IO.retrieve(s"$source/$shard", s"$target/temp_$shard")
      IO.index(s"$target/temp_$shard", s"$target/shards/$shard")
      IO.delete(s"$target/temp_$shard")
      // confirm we're done
      sender ! ()
  }
}