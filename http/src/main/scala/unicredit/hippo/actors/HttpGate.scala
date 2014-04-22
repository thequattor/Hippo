package unicredit.hippo
package actors

import scala.concurrent.duration._

import akka.actor.{ Actor, ActorRef, ActorLogging }
import akka.util.Timeout
import akka.pattern.ask
import spray.routing.HttpService
import spray.http.HttpMethods._
import spray.httpx.Json4sJacksonSupport
import org.json4s.NoTypeHints
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.jackson.Serialization.formats
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._

import messages.{ Request, Result, GetSiblings, Siblings }


object Support extends Json4sJacksonSupport {
  implicit val json4sJacksonFormats = formats(NoTypeHints)
}

class HttpGate(client: ActorRef) extends Actor with ActorLogging with HttpService {
  private val config = ConfigFactory.load
  private val duration = config.as[FiniteDuration]("request.timeout")
  implicit val timeout = Timeout(duration)

  def actorRefFactory = context
  import context.dispatcher
  import Support._

  def receive = runRoute {
    get {
      path("query") {
        parameterMultiMap { params ⇒
          val message = Request(
            table = params("table").head,
            keys = params("key"),
            columns = params("column")
          )

          complete {
            (client ? message).mapTo[Result] map { x ⇒ render(x.content) }
          }
        }
      } ~
      path("nodes") {
        complete {
          (client ? GetSiblings).mapTo[Map[String, ActorRef]] map { x ⇒ render(x.keySet.toList) }
        }
      }
    }
  }
}