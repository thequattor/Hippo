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

package unicredit.hippo.pattern

import scala.concurrent.Future

import akka.actor.ActorRef
import akka.pattern.{ ask, AskTimeoutException }
import akka.util.Timeout


trait FixedAskSupport {
  // Modifies the semantics of the standard ask pattern
  // in such a way that a timeout results in a failed future
  // rather than throwing an exception.
  //
  // To use it, just change
  //
  //   import akka.pattern.ask
  //
  // to
  //
  //   import import unicredit.hippo.pattern.fixedAsk
  implicit def fixedAsk(actorRef: ActorRef) = new AskableActorRef(actorRef)
}

final class AskableActorRef(val actorRef: ActorRef) extends AnyVal {
  def ?(message: Any)(implicit timeout: Timeout) = try {
    ask(actorRef).?(message)(timeout)
  } catch {
    case e: AskTimeoutException ⇒ Future.failed(e)
  }
}