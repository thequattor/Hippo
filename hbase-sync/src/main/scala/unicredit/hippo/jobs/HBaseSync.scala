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

  val fields = new Fields(columns: _*)

  private val order = implicitly[Ordering[String]].reverse
  private def hash(s: String) = s.md5.hex
  private val joiner = new TrivialJoiner("𐒉") // OSMANYA LETTER SHIIN (10489)

  val input = HBaseSource(
    table,
    quorum,
    "key",
    List.fill(columns.length)(columnFamily),
    columns
  )
  val output = TemplatedTextSequenceFile(
    basePath = args("output"),
    template = "%s/shards/%s",
    pathFields = ('server, 'partition),
    fields = ('key, 'value)
  )

  input.readStrings
    .flatMap('key -> 'server) { key: String => shards(key, servers, replicas) }
    .map('key -> 'partition) { key: String => shard(key, partitions) }
    .map(fields -> 'all) { tuple: Tuple =>
      tuple.iterator.toList.asInstanceOf[List[String]]
    }
    .groupBy('server, 'partition) {
      _.toList[(String, Tuple)](('key, 'all) -> 'fields)
    }
    .flatMap('fields -> ('key, 'value)) { xs: List[(String, List[String])] =>
      for {
        (key, fields) <- xs
        (column, value) <- (columns, fields).zipped
        if value != null
      } yield (new Text(joiner.join(key, column)), new Text(value))
    }
    .project('server, 'partition, 'key, 'value)
    .write(output)
}