package unicredit.hippo.retriever

import org.apache.hadoop.io.{ Text, SequenceFile, MapFile }
import org.apache.hadoop.fs.{ Path, FileUtil }
import org.apache.hadoop.conf.Configuration


object IO {
  // Reads a remote directory on HDFS, merging the
  // files inside it
  def retrieve(in: String, out: String) = {
    val conf = new Configuration
    val input = new Path(in)
    val output = new Path(out)

    FileUtil.copyMerge(input.getFileSystem(conf), input, output.getFileSystem(conf),
      output, false, conf, "")
  }

  // Reads file names from a remote directory on HDFS
  def listFiles(dir: String) = {
    val conf = new Configuration
    val path = new Path(dir)
    val fs = path.getFileSystem(conf)
    val iter = fs.listLocatedStatus(path)
    var names = List.empty[String]
    while (iter.hasNext) { names ::= iter.next.getPath.getName }
    names
  }

  // Converts a SequenceFile to a MapFile, thus
  // sorting it and adding an index
  def index(in: String, out: String) = {
    val conf = new Configuration
    val input = new Path(in)
    val dir = new Path(out)
    val output = new Path(s"$out/data")
    val fs = input.getFileSystem(conf)
    val sorter = new SequenceFile.Sorter(fs, classOf[Text], classOf[Text], conf)

    // The difference between any SequenceFile and a MapFile
    // is that the latter is sorted...
    sorter.sort(input, output)
    // ...and has an index
    MapFile.fix(fs, dir, classOf[Text], classOf[Text], false, conf)
  }
}