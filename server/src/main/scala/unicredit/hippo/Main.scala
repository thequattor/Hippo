package unicredit.hippo

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import akka.contrib.pattern.ClusterReceptionistExtension
import spray.can.Http
import com.typesafe.config.ConfigFactory

import actors._
import storage.Repository


object Main extends App {
  private val config = ConfigFactory.load
  private val home = config getString "storage.home"
  private val host = config getString "http.hostname"
  private val port = config getInt "http.port"

  implicit val system = ActorSystem("hippo")
  val retriever = system.actorOf(
    Props(new Retriever(home)),
    name = "retriever"
  )
  val frontend = system.actorOf(
    Props(new Frontend(retriever)),
    name = "frontend"
  )
  val http = system.actorOf(
    Props(new HttpGate(frontend)),
    name = "http"
  )

  ClusterReceptionistExtension(system).registerService(frontend)
  IO(Http) ! Http.Bind(http, interface = host, port = port)
}