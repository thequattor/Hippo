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

package unicredit.hippo.jobs

import scala.collection.JavaConversions._

import cascading.tuple.{ Tuple, Fields }
import com.twitter.scalding._
import com.roundeights.hasher.Implicits._
import org.apache.hadoop.io.Text

import unicredit.hippo.source.{ HBaseSource, TemplatedTextSequenceFile }
import unicredit.hippo.storage.TrivialJoiner
import unicredit.hippo.common.{ shard, shards }


class HBaseSync(args: Args) extends Job(args) {
  val columnFamily = args("cf")
  val columns = args("columns") split ',' toList
  val replicas = args("replicas").toInt
  val partitions =  1 to args("partitions").toInt map (_.toString)
  val servers = args("servers") split ',' toList
  val table = args("table")
  val quorum = args("quorum")
  val out = args("output")

  val fields = new Fields(columns: _*)

  private val order = implicitly[Ordering[String]].reverse
  private def hash(s: String) = s.md5.hex
  private val joiner = new TrivialJoiner("𐒉") // OSMANYA LETTER SHIIN (10489)

  val input = HBaseSource(
    table,
    quorum,
    "id",
    List.fill(columns.length)(columnFamily),
    columns
  )
  val output = TemplatedTextSequenceFile(
    basePath = s"$out/$table/",
    template = "%s/shards/%s",
    pathFields = ('server, 'partition),
    fields = ('key, 'value)
  )

  input.readStrings
    .flatMap('id -> 'server) { id: String => shards(id, servers, replicas) }
    .map('id -> 'partition) { id: String => shard(id, partitions) }
    .map(fields -> 'all) { tuple: Tuple =>
      tuple.iterator.toList.asInstanceOf[List[String]]
    }
    .flatMap(('id, 'all) -> ('key, 'value)) { xs: (String, List[String]) =>
      val (key, fields) = xs

      for {
        (column, value) <- (columns, fields).zipped
        if value != null
      } yield (new Text(joiner.join(key, column)), new Text(value))
    }
    .project('server, 'partition, 'key, 'value)
    .write(output)
}