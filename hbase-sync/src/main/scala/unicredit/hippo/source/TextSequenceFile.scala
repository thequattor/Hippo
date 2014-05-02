/*  Copyright 2014 UniCredit S.p.A.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package unicredit.hippo.source

import cascading.tap.Tap
import cascading.tuple.{ Tuple, Fields }
import cascading.flow.FlowProcess
import cascading.scheme.SinkCall
import cascading.scheme.hadoop.SequenceFile
import org.apache.hadoop.mapred.{ JobConf, OutputCollector, RecordReader, SequenceFileOutputFormat }
import org.apache.hadoop.io.Text

// This is essentially the same as
//
//   cascading.scheme.hadoop.SequenceFile
//
// but uses Text keys and values instead of Tuple.
// Moreover, the default cascading implementation
// assumes any number of fields, and stores an empty
// key, and the whole tuple as value. Instead, this
// implementation will only work for pipes of tuples
// containing exactly two fields: the first to be used
// as a key, and the other one as value.
//
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