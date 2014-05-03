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

package unicredit.hippo.retriever

import org.apache.hadoop.io.{ Text, SequenceFile, MapFile }
import org.apache.hadoop.fs.{ Path, FileUtil }
import org.apache.hadoop.conf.Configuration


object IO {
  // Reads a remote directory on HDFS, merging the
  // files inside it
  def retrieve(in: String, out: String) = {
    val conf = new Configuration
    val input = new Path(in)
    val output = new Path(out)

    FileUtil.copyMerge(input.getFileSystem(conf), input, output.getFileSystem(conf),
      output, false, conf, "")
  }

  // Creates a directory, either locally or on HDFS
  def mkdir(dir: String) = {
    val conf = new Configuration
    val path = new Path(dir)
    val fs = path.getFileSystem(conf)

    fs.mkdirs(path)
  }

  // Removes a file, either locally or on HDFS
  def delete(file: String) = {
    val conf = new Configuration
    val path = new Path(file)
    val fs = path.getFileSystem(conf)

    fs.delete(path, false) // not recursive
  }

  // Reads file names from a remote directory on HDFS
  def listFiles(dir: String) = {
    val conf = new Configuration
    val path = new Path(dir)
    val fs = path.getFileSystem(conf)
    val iter = fs.listLocatedStatus(path)
    var names = List.empty[String]
    while (iter.hasNext) { names ::= iter.next.getPath.getName }
    names
  }

  // Converts a SequenceFile to a MapFile, thus
  // sorting it and adding an index
  def index(in: String, out: String) = {
    val conf = new Configuration
    val input = new Path(in)
    val dir = new Path(out)
    val output = new Path(s"$out/data")
    val fs = input.getFileSystem(conf)
    val sorter = new SequenceFile.Sorter(fs, classOf[Text], classOf[Text], conf)

    // The difference between any SequenceFile and a MapFile
    // is that the latter is sorted...
    sorter.sort(input, output)
    // ...and has an index
    MapFile.fix(fs, dir, classOf[Text], classOf[Text], false, conf)
  }
}