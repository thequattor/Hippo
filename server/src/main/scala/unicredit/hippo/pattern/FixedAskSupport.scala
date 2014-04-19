package unicredit.hippo.pattern

import scala.concurrent.Future

import akka.actor.ActorRef
import akka.pattern.{ ask, AskTimeoutException }
import akka.util.Timeout


trait FixedAskSupport {
  // Modifies the semantics of the standard ask pattern
  // in such a way that a timeout results in a failed future
  // rather than throwing an exception.
  //
  // To use it, just change
  //
  //   import akka.pattern.ask
  //
  // to
  //
  //   import import unicredit.hippo.pattern.fixedAsk
  implicit def fixedAsk(actorRef: ActorRef) = new AskableActorRef(actorRef)
}

final class AskableActorRef(val actorRef: ActorRef) extends AnyVal {
  def ?(message: Any)(implicit timeout: Timeout) = try {
    ask(actorRef).?(message)(timeout)
  } catch {
    case e: AskTimeoutException => Future.failed(e)
  }
}