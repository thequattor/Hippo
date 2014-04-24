package unicredit.hippo
package retriever
package actors

import akka.actor.{ Actor, ActorLogging }

import messages.Download


class Downloader extends Actor with ActorLogging {
  def receive = {
    case Download(source, target) ⇒
      IO.retrieve(source, s"$target/temp")
      IO.index(s"$target/temp", target)
      // confirm we're done
      sender ! ()
  }
}