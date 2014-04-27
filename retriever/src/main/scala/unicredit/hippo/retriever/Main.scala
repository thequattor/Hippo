package unicredit.hippo
package retriever

import scala.concurrent.duration._

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import actors.Downloader
import messages.Download


object Main extends App {
  implicit val timeout = Timeout(10 minutes)
  val system = ActorSystem("hippo-retriever")
  val downloader = system.actorOf(
    Props[Downloader],
    name = "downloader"
  )
  import system.dispatcher

  Parser.parse(args, { config ⇒
    val message = Download(config.source, config.table, config.id, config.target)

    (downloader ? message) onComplete { _ ⇒
      system.shutdown
    }
  })
}