package unicredit.hippo.pattern

import scala.concurrent.{ Future, ExecutionContext }


trait FallbackSupport {
  // Enhances Future[Any] with a method `orElse` which
  // does the same as `withFallback`, but is lazy in
  // its argument
  implicit def fallback(future: Future[Any]) = new FallbackFuture(future)
}

final class FallbackFuture(val future: Future[Any]) extends AnyVal {
  def orElse(alternative: ⇒ Future[Any])(implicit ec: ExecutionContext) =
    future recoverWith { case _: Throwable ⇒ alternative }
}