package unicredit.hippo
package retriever

import scala.concurrent.duration._
import scala.concurrent.Future

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
    val baseDir = s"${ config.source }/${ config.table }/${ config.id }/shards"
    val shards = IO.listFiles(baseDir)

    val futures = for {
      shard <- shards
      message = Download(baseDir, config.target, shard)
    } yield downloader ? message

    Future.sequence(futures) onComplete { _ ⇒
      system.shutdown
    }
  })
}