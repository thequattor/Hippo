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

package unicredit.hippo.storage

trait Joiner {
  def join(a: String, b: String): String
  def split(x: String): (String, String)
}

class TrivialJoiner(separator: String) extends Joiner {
  def join(key: String, column: String) =
    s"$key$separator$column"

  def split(x: String) = x split separator match {
    case Array(a, b) ⇒ (a, b)
    case _ ⇒ sys.error(s"Not a valid encoded string: $x")
  }

}