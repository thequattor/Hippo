package unicredit.hippo
package retriever
package actors

import akka.actor.{ Actor, ActorLogging }

import messages.Download


class Downloader extends Actor with ActorLogging {
  def receive = {
    case Download(source, table, id, target) ⇒
      val baseDir = s"$source/$table/$id/shards"
      val shards = IO.listFiles(baseDir)

      for (shard <- shards) {
        IO.retrieve(s"$baseDir/$shard", s"$target/temp_$shard")
        IO.index(s"$target/temp_$shard", s"$target/shards/$shard")
      }
      // confirm we're done
      sender ! ()
  }
}