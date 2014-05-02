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

import com.roundeights.hasher.Implicits._


package object common {
  private val order = implicitly[Ordering[String]].reverse
  private def hash(s: String) = s.md5.hex

  def shard(key: String, indices: Seq[String]) =
    indices maxBy { i ⇒ hash(key + i) }

  def shards(key: String, indices: Seq[String], n: Int) =
    indices.sortBy({ i ⇒ hash(key + i) })(order) take n toList
}