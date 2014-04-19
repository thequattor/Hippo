package unicredit.hippo

import com.roundeights.hasher.Implicits._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path


package object util {
  private val order = implicitly[Ordering[String]].reverse
  private def hash(s: String) = s.md5.hex

  def shard(key: String, indices: Seq[String]) =
    indices maxBy { i => hash(key + i) }

  def shards(key: String, indices: Seq[String], n: Int) =
    indices.sortBy({ i => hash(key + i) })(order) take n toList

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