package unicredit.hippo.source

import scala.util.control.Exception.allCatch
import scala.collection.mutable.Buffer

import cascading.scheme.Scheme
import cascading.tap.{ Tap, SinkMode }
import cascading.tuple._
import cascading.flow.FlowDef
import cascading.scheme.local.{ TextLine => CLTextLine }
import cascading.scheme.hadoop.{ TextLine => CHTextLine }
import org.apache.hadoop.mapred.{ JobConf, RecordReader, OutputCollector }
import com.twitter.maple.hbase.{ HBaseTap, HBaseScheme }
import com.twitter.scalding._
import com.twitter.scalding.Dsl._


case class HBaseSource(tableName: String,
  quorum: String = "localhost",
  keyName: String,
  familyNames: List[String],
  columnNames: List[String]) extends Source {

  def name = tableName

  val keyFields = new Fields(keyName)
  val columnFields = columnNames map { n => new Fields(n) }
  val allFields = keyFields :: columnFields

  def readStrings(implicit flowDef: FlowDef, mode: Mode) =
    read(flowDef, mode).asString(allFields)

  val hdfsScheme = new HBaseScheme(keyFields, familyNames.toArray, columnFields.toArray)
    .asInstanceOf[Scheme[JobConf, RecordReader[_, _], OutputCollector[_, _], _, _]]

  type TestConfig
  type TestSourceContext
  type TestSinkContext

  def localScheme = new CLTextLine(new Fields("offset", "line"), Fields.ALL, textEncoding)

  val textEncoding: String = CHTextLine.DEFAULT_CHARSET
  val testScheme = new HBaseTestScheme[TestConfig, Buffer[Tuple], TestSourceContext, TestSinkContext](keyFields, familyNames.toArray, columnFields.toArray)

  override def createTap(readOrWrite: AccessMode)(implicit mode: Mode): Tap[_, _, _] = {
    val hBaseScheme = hdfsScheme match {
      case hbase: HBaseScheme => hbase
      case _ => throw new ClassCastException("Failed casting from Scheme to HBaseScheme")
    }

    mode match {
      case hdfsMode @ Hdfs(_, _) => readOrWrite match {
        case Read => {
          new HBaseTap(quorum, tableName, hBaseScheme, SinkMode.KEEP)
        }
        case Write => {
          new HBaseTap(quorum, tableName, hBaseScheme, SinkMode.UPDATE)
        }
      }

      case _ => {
        allCatch.opt(
          TestTapFactory(this, hBaseScheme, SinkMode.REPLACE)).map {
            _.createTap(readOrWrite) // these java types are invariant, so we cast here
              .asInstanceOf[Tap[Any, Any, Any]]
          }
          .orElse {
            allCatch.opt(
              TestTapFactory(this, localScheme.getSourceFields, SinkMode.REPLACE)).map {
                _.createTap(readOrWrite)
                  .asInstanceOf[Tap[Any, Any, Any]]
              }
          }.getOrElse(sys.error("Failed to create a tap for: " + toString))
      }
    }
  }
}