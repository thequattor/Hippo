package unicredit.hippo.storage

import org.apache.hadoop.io.{ MapFile, Text, IOUtils }
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem


class IO(path: String) {
  private val conf = new Configuration
  private val fs = FileSystem.get(conf)
  private val txt = classOf[Text]

  def write(data: Map[String, String]) = {
    val key = new Text
    val value = new Text
    val writer = new MapFile.Writer(conf, fs, path, txt, txt)

    for { (k, v) <- data.toSeq sortBy (_._1) } {
      key.set(k)
      value.set(v)
      writer.append(key, value)
    }
    IOUtils.closeStream(writer)
  }

  def read(keys: Seq[String]) = {
    val reader = new MapFile.Reader(fs, path, conf)
    var result = Map.empty[String, String]

    for { k <- keys } {
      val key = new Text(k)
      val value = new Text

      reader.get(key, value)
      result += (k -> value.toString)
    }
    IOUtils.closeStream(reader)

    result
  }
}
