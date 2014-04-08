package unicredit.hippo

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory

import actors._
import storage.Repository


object Main extends App {
  private val config = ConfigFactory.load
  private val partitions = config getInt "storage.partitions"
  private val home = config getString "storage.home"
  private val host = config getString "http.hostname"
  private val port = config getInt "http.port"
  // val repo = new Repository(home, "people", 12, "abcd123")
  // def write = {
  //   repo.write("firry", Map("name" -> "Marco", "surname" -> "Firrincieli"))
  //   repo.write("ferrets", Map("name" -> "Andrea", "surname" -> "Ferretti", "age" -> "33"))
  //   repo.write("pazqo", Map("name" -> "Stefano", "surname" -> "Pascolutti"))
  // }

  implicit val system = ActorSystem("hippo")
  val retriever = system.actorOf(
    Props(new Retriever(home, partitions)),
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

  IO(Http) ! Http.Bind(http, interface = host, port = port)

  readLine("Press <Enter> to shutdown...")
  system.shutdown
}