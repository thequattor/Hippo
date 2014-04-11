package unicredit.hippo.sharding

import com.roundeights.hasher.Implicits._


class RemoteShard {
  val order = implicitly[Ordering[String]].reverse

  def hash(s: String) = s.md5.hex

  def indicesFor(key: String, indices: Seq[String], n: Int) =
    indices.sortBy({ i => hash(key + i) })(order) take n toList
}
