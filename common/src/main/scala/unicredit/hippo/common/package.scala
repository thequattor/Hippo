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