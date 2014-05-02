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