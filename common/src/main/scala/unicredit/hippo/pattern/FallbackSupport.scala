package unicredit.hippo.pattern

import scala.concurrent.{ Future, ExecutionContext }


trait FallbackSupport {
  // Enhances Future[A] with a method `orElse` which
  // does the same as `withFallback`, but is lazy in
  // its argument
  implicit def fallback[A](future: Future[A]) = new FallbackFuture[A](future)
}

final class FallbackFuture[A](val future: Future[A]) extends AnyVal {
  def orElse(alternative: ⇒ Future[A])(implicit ec: ExecutionContext): Future[A] = {
    val pf: PartialFunction[Throwable, Future[A]] =
      PartialFunction({ _: Throwable ⇒ alternative })

    // I would like to write
    //
    //   future recoverWith { case _: Throwable ⇒ alternative }
    //
    // but God knows why this does not typecheck. :-?
    future recoverWith pf
  }
}