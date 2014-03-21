package unicredit.hippo
package storage

import scala.util.control.Exception.allCatch

import org.apache.hadoop.fs.Path
import scalaz._
import Scalaz._

import sharding.Shard


class Repository(table: String, partitions: Int, version: String) {
  private val shard = new Shard(table, partitions, version)
  private val joiner = new TrivialJoiner("𐒉") // OSMANYA LETTER SHIIN (10489)

  private def uri(key: String) =
    new Path(shard.uriForKey(key)).toString

  private def accumulate[A, B](maps: Iterable[Map[A, Map[B, String]]]) =
    maps.foldLeft(Map.empty[A, Map[B, String]]) (_ |+| _)

  private def decode(result: Map[String, String]) =
    result.toList map { case (key, value) =>
      val (k, c) = joiner.split(key)
      Map(k -> Map(c -> value))
    }

  def write(key: String, data: Map[String, String]) =
    new IO(uri(key)) write (data map { case (col, value) =>
      joiner.join(key, col) -> value
    })

  def read(keys: Seq[String], columns: Seq[String]) = {
    val shards = keys groupBy uri map { case (path, ks) =>
      val result = new IO(path) read (for {
        k <- ks
        c <- columns
      } yield joiner.join(k, c))

      result |> decode |> accumulate
    }

    shards |> accumulate
  }
}
