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

import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes
import cascading.pipe.Pipe
import cascading.tuple.Fields
import cascading.tuple.Fields._
import cascading.flow.FlowDef
import com.twitter.scalding.Dsl._
import com.twitter.scalding.Mode


package object source {
  implicit class HBasePipe(val p: Pipe) extends AnyVal {
    def asString(f: Fields): Pipe =
      asList(f).foldLeft(p) { (oldPipe, fld) =>
        oldPipe.map(fld.toString() -> fld.toString()) { str: ImmutableBytesWritable =>
          Bytes.toString(str.get)
        }
      }

    def asBytes(f: Fields): Pipe =
      asList(f).foldLeft(p) { (oldPipe, fld) =>
        oldPipe.map(fld.toString() -> fld.toString()) { str: String =>
          val string = Option(str) getOrElse ""

          new ImmutableBytesWritable(string.getBytes)
        }
      }

    def writeBytes(out: HBaseSource)(implicit flowDef: FlowDef, mode: Mode) =
      asBytes(out.allFields).write(out)(flowDef, mode)
  }
}