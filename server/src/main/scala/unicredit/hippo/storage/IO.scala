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

import org.apache.hadoop.io.{ MapFile, Text, IOUtils }
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path


class IO(path: String) {
  private val conf = new Configuration

  def read(keys: Seq[String]) = {
    val reader = new MapFile.Reader(new Path(path), conf)
    var result = Map.empty[String, String]

    for { k <- keys } {
      val key = new Text(k)
      val value = new Text

      if (reader.get(key, value) != null) {
        result += (k -> value.toString)
      }
    }
    IOUtils.closeStream(reader)

    result
  }
}
