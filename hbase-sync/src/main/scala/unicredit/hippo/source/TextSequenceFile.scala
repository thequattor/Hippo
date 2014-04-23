package unicredit.hippo.source

import cascading.tap.Tap
import cascading.tuple.{ Tuple, Fields }
import cascading.flow.FlowProcess
import cascading.scheme.SinkCall
import cascading.scheme.hadoop.SequenceFile
import org.apache.hadoop.mapred.{ JobConf, OutputCollector, RecordReader, SequenceFileOutputFormat }
import org.apache.hadoop.io.Text


// Only made the necessary modifications to work as sink
class TextSequenceFile(fields: Fields) extends SequenceFile(fields) {
  override def sinkConfInit(
    flowProcess: FlowProcess[JobConf],
    tap: Tap[JobConf, RecordReader[_, _], OutputCollector[_, _]],
    conf: JobConf) = {
      conf.setOutputKeyClass((new Text).getClass)
      conf.setOutputValueClass((new Text).getClass)
      conf.setOutputFormat(classOf[SequenceFileOutputFormat[Text, Text]])
    }

  override def sink(
    flowProcess: FlowProcess[JobConf],
    sinkCall: SinkCall[Void, OutputCollector[_, _]] ) = {
      val iterator = sinkCall.getOutgoingEntry.getTuple.iterator
      val key = iterator.next.asInstanceOf[Text]
      val value = iterator.next.asInstanceOf[Text]

      sinkCall.getOutput.asInstanceOf[OutputCollector[Text, Text]].collect(key, value)
  }
}