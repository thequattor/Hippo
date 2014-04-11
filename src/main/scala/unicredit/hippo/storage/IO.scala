package unicredit.hippo.storage

import org.apache.hadoop.io.{ MapFile, Text, IOUtils }
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path


class IO(path: String) {
  private val conf = new Configuration

  def read(keys: Seq[String]) = {
    val reader = new MapFile.Reader(new Path(path), conf)
    var result = Map.empty[String, String]

    for { k <- keys } {
      val key = new Text(k)
      val value = new Text

      if (reader.get(key, value) != null) {
        result += (k -> value.toString)
      }
    }
    IOUtils.closeStream(reader)

    result
  }
}
