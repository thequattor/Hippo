package unicredit.hippo.jobs

import scala.collection.JavaConversions._

import cascading.tuple.{ Tuple, Fields }
import com.twitter.scalding._
import com.roundeights.hasher.Implicits._

import unicredit.hippo.source.HBaseSource
import unicredit.hippo.storage.TrivialJoiner


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

  def shard(key: String, indices: Seq[String]) =
    indices maxBy { i ⇒ hash(key + i) }

  def shards(key: String, indices: Seq[String], n: Int) =
    indices.sortBy({ i ⇒ hash(key + i) })(order) take n toList

  val input = HBaseSource(
    table,
    quorum,
    "key",
    List.fill(columns.length)(columnFamily),
    columns
  )
  val output = new TemplatedSequenceFile(
    basePath = args("output"),
    template = "%s/shards/%s",
    pathFields = ('server, 'partition)
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
      } yield (joiner.join(key, column), value)
    }
    .write(output)
}