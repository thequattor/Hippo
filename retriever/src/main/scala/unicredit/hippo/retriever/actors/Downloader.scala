package unicredit.hippo
package retriever
package actors

import akka.actor.{ Actor, ActorLogging }

import messages.Download


class Downloader extends Actor with ActorLogging {
  def receive = {
    case Download(source, target, shard) ⇒
      IO.retrieve(s"$source/$shard", s"$target/temp_$shard")
      IO.index(s"$target/temp_$shard", s"$target/shards/$shard")
      // confirm we're done
      sender ! ()
  }
}