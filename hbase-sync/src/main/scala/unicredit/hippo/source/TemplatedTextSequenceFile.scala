package unicredit.hippo.source

import cascading.tuple.Fields
import cascading.tap.SinkMode
import com.twitter.scalding.TemplateSource


case class TemplatedTextSequenceFile(
  override val basePath: String,
  override val template: String,
  override val fields: Fields,
  override val pathFields: Fields,
  override val sinkMode: SinkMode = SinkMode.REPLACE
) extends TemplateSource with TextSequenceFileScheme