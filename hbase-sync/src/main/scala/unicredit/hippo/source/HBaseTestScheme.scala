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

import scala.collection.mutable
import scala.collection.JavaConversions._

import cascading.flow.FlowProcess
import cascading.scheme.{ConcreteCall, SourceCall, SinkCall, Scheme}
import cascading.tap.Tap
import cascading.tuple._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes


class HBaseTestScheme[A, B <: mutable.Buffer[Tuple], C, D] (keyField: Fields, familyNames: Array[String], columnFields: Array[Fields])
  extends Scheme[A, B, B, C, D] {

  require (keyField.size == 1, "HBaseTestScheme may only have one key field, found: " + keyField.print)

  var allFields = keyField
  if (columnFields.length != 0) {
    allFields = columnFields.foldLeft(keyField)((f, x) => Fields.join(f, x))
  }
  setSourceFields(allFields)
  setSinkFields(allFields)

  def sourceConfInit(flowProcess: FlowProcess[A], tap: Tap[A, B, B], config: A) {}
  def sinkConfInit(flowProcess: FlowProcess[A], tap: Tap[A, B, B], config: A) {}

  def source(flowProcess: FlowProcess[A], sourceCall: SourceCall[C, B]): Boolean = {

    val input: Tuple = sourceCall.getIncomingEntry.getTuple
    val result: Tuple = new Tuple

    try {
      input.foreach(f => result.add(new ImmutableBytesWritable(f.toString.getBytes)))
    }
    catch {
      case e : Exception => throw new IllegalArgumentException("Error \"" + e.getMessage + "\" when trying to source the test Tuple(" + input + "). Please check if the input value is a Tuple" + allFields.size + " of values.")
    }

    sourceCall.getIncomingEntry.setTuple(result)

    true
  }

  def sink(flowProcess: FlowProcess[A], sinkCall: SinkCall[D, B]) {
    val tupleEntry = sinkCall.getOutgoingEntry
    val result: Tuple = new Tuple

    try {
      for(f <- tupleEntry.getFields.iterator.toList.asInstanceOf[List[String]]) {
        result.add( Bytes.toString( tupleEntry.getObject(f).asInstanceOf[ImmutableBytesWritable].get()) )
      }
    }
    catch {
      case e : Exception => throw new UnsupportedOperationException("Error \"" + e.getMessage + "\" when trying to sink the output Tuple(" + tupleEntry.getTuple + ") for testing assertions. Please check if the flow outputs Tuple" + allFields.size + " of ImmutableBytesWritable values.")
    }

    val output = sinkCall.getOutput
    output.append(result)
  }

}

class HBaseTestTap[A, B <: mutable.Buffer[Tuple]](val quorum: String, val tableName: String, scheme: HBaseTestScheme[A, B, _ , _], val buffer : B)
  extends Tap[A, B, B](scheme.asInstanceOf[Scheme[A, B, B, _, _]])  {

  override def createResource(jobConf: A) = true
  override def deleteResource(jobConf: A) = true
  override def resourceExists(jobConf: A) = true
  override def getModifiedTime(jobConf: A) = 1L
  override def getIdentifier: String = scala.math.random.toString

  override def openForRead(flowProcess : FlowProcess[A], input : B) = {
    new HBaseTestIterator(flowProcess, scheme, buffer)
  }

  override def openForWrite(flowProcess : FlowProcess[A], output : B) : TupleEntryCollector = {
    new HBaseTestCollector(flowProcess, scheme, buffer)
  }

  override def equals(other : Any) = this.eq(other.asInstanceOf[AnyRef])

  override def hashCode() = System.identityHashCode(this)
}

class HBaseTestIterator[A, B <: mutable.Buffer[Tuple], C] (val flowProcess: FlowProcess[A], val scheme : HBaseTestScheme[A, B, C, _], val inputBuffer : B) extends TupleEntryIterator(scheme.getSourceFields) {

  var pos = 0
  val sourceCall : ConcreteCall[C, B] = new ConcreteCall()

  sourceCall.setInput( inputBuffer )

  def hasNext: Boolean = inputBuffer.size > pos

  def next(): TupleEntry = {

    val tupleEntry = getTupleEntry
    tupleEntry.setTuple(inputBuffer(pos))

    assert(tupleEntry.getFields.size == tupleEntry.getTuple.size, "Unexpected number of elements in test Tuple(\"" + tupleEntry.getTuple + "\"). Please provide a Tuple" + tupleEntry.getFields.size + " of strings." )

    sourceCall.setIncomingEntry( tupleEntry )
    scheme.source( flowProcess, sourceCall )

    pos = pos + 1
    tupleEntry
  }
  def remove() {}
  def close() {}
}

class HBaseTestCollector[A, B <: mutable.Buffer[Tuple], D](val flowProcess: FlowProcess[A], val scheme : HBaseTestScheme[A, B, _, D], val outputBuffer : B) extends TupleEntryCollector(scheme.getSourceFields) {

  val sinkCall : ConcreteCall[D, B] = new ConcreteCall()
  sinkCall.setOutput(outputBuffer)

  override def collect(tupleEntry : TupleEntry) {
    sinkCall.setOutgoingEntry( tupleEntry )
    scheme.sink( flowProcess, sinkCall )
  }
}