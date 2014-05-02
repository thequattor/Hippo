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

package unicredit.hippo.retriever

import java.io.File
import scopt.OptionParser

object Parser {
  case class Config(
    source: String = "",
    table: String = "",
    id: String = "",
    target: String = ""
  )

  val parser = new OptionParser[Config]("hippo-db retriever") {
    head("benchmark parser", "0.1")
    opt[String]('s', "source") valueName("<path>") action { (x, c) =>
      c.copy(source = x)
    } text("the directory to retrieve from HDFS")
    opt[String]('p', "table") valueName("<name>") action { (x, c) =>
      c.copy(source = x)
    } text("the table to retrieve")
    opt[String]('i', "id") valueName("<name>") action { (x, c) =>
      c.copy(id = x)
    } text("the local id of this server")
    opt[String]('t', "target") valueName("<path>") action { (x, c) =>
      c.copy(target = x)
    } text("the local destination")
    help("help") text("prints this usage text")
  }

  def parse(args: Seq[String], action: Config => Unit) = {
    parser.parse(args, Config()) map action
  }
}
