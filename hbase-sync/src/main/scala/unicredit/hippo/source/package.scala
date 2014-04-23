package unicredit.hippo

import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes
import cascading.pipe.Pipe
import cascading.tuple.Fields
import cascading.tuple.Fields._
import cascading.flow.FlowDef
import com.twitter.scalding.Dsl._
import com.twitter.scalding.Mode


package object source {
  implicit class HBasePipe(val p: Pipe) extends AnyVal {
    def asString(f: Fields): Pipe =
      asList(f).foldLeft(p) { (oldPipe, fld) =>
        oldPipe.map(fld.toString() -> fld.toString()) { str: ImmutableBytesWritable =>
          Bytes.toString(str.get)
        }
      }

    def asBytes(f: Fields): Pipe =
      asList(f).foldLeft(p) { (oldPipe, fld) =>
        oldPipe.map(fld.toString() -> fld.toString()) { str: String =>
          val string = Option(str) getOrElse ""

          new ImmutableBytesWritable(string.getBytes)
        }
      }

    def writeBytes(out: HBaseSource)(implicit flowDef: FlowDef, mode: Mode) =
      asBytes(out.allFields).write(out)(flowDef, mode)
  }
}