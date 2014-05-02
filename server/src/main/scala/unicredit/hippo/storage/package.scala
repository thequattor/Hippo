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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path


package object storage {
  // This rather low-level function reads a
  // small UTF-8 encoded file and returns a
  // string with its content.
  //
  // It is needed, in place of the more idiomatic
  //
  //   scala.io.Resource.fromFile(file).mkString
  //
  // when the file may reside either on the local
  // filesystem or on HDFS.
  def read(file: String) = {
    val path = new Path(file)
    val fs = path.getFileSystem(new Configuration)
    var result = ""
    var stream: java.io.DataInputStream = null //D'oh!
    try {
      stream = fs.open(path)
      val length = stream.available
      val buffer = Array.fill[Byte](length)(0)

      stream.read(buffer)
      result = new String(buffer)
    }
    finally {
      stream.close
    }
    result
  }

  // This rather low-level function reads the list
  // of child names of a given directory. It is needed
  // when the directory may reside either on the local
  // filesystem or on HDFS.
  def children(dir: String) = {
    val path = new Path(dir)
    val fs = path.getFileSystem(new Configuration)
    val iter = fs.listLocatedStatus(path)
    var names = List.empty[String]
    while (iter.hasNext) { names ::= iter.next.getPath.getName }
    names
  }
}