package unicredit.hippo.retriever

import org.apache.hadoop.io.{ SequenceFile, MapFile }
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.Text


object Converter {
  def seq2mapFile(in: String, out: String) = {
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