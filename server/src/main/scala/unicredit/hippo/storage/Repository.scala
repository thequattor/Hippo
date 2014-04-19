package unicredit.hippo
package storage

import scala.util.control.Exception.allCatch

import scalaz._
import Scalaz._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

import common.shard


class Repository(home: String, table: String, version: String) {
  private val joiner = new TrivialJoiner("𐒉") // OSMANYA LETTER SHIIN (10489)
  private val path = s"$home/$table/$version/shards/"
  private val indices = children(path)

  private def uri(key: String) = s"$path/${ shard(key, indices) }"

  private def accumulate[A, B](maps: Iterable[Map[A, Map[B, String]]]) =
    maps.foldLeft(Map.empty[A, Map[B, String]]) (_ |+| _)

  private def decode(result: Map[String, String]) =
    result.toList map { case (key, value) =>
      val (k, c) = joiner.split(key)
      Map(k -> Map(c -> value))
    }

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
