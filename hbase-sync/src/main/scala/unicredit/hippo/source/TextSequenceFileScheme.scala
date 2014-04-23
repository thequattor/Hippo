package unicredit.hippo.source

import cascading.tuple.Fields
import cascading.scheme.Scheme
import com.twitter.scalding.SchemedSource
import org.apache.hadoop.mapred.{ JobConf, OutputCollector, RecordReader }


trait TextSequenceFileScheme extends SchemedSource {
  def fields: Fields

  override def hdfsScheme =
    new TextSequenceFile(fields).asInstanceOf[Scheme[JobConf, RecordReader[_, _], OutputCollector[_, _], _, _]]
}