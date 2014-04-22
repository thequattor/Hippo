package unicredit.hippo

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._

import actors.{ Client, HttpGate }


object Main extends App {
  private val config = ConfigFactory.load
  private val host = config.as[String]("http.hostname")
  private val port = config.as[Int]("http.port")
  private val servers = config.as[List[String]]("hippo.servers")

  implicit val system = ActorSystem("hippo-http")
  val client = system.actorOf(
    Props(new Client(servers)),
    name = "client"
  )
  val http = system.actorOf(
    Props(new HttpGate(client)),
    name = "http"
  )

  IO(Http) ! Http.Bind(http, interface = host, port = port)
}