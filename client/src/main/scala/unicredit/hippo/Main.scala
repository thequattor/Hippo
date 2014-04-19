package unicredit.hippo

import scala.collection.JavaConversions._

import akka.actor.{ ActorSystem, Props }
import com.typesafe.config.ConfigFactory

import actors.Client


object Main extends App {
  private val config = ConfigFactory.load
  private val contacts = config.getStringList("hippo.seed-nodes")

  val system = ActorSystem("hippo-client")

  val client = system.actorOf(
    Props(new Client(contacts)),
    name = "retriever"
  )

  import system.dispatcher
  import scala.concurrent.duration._

  system.scheduler.scheduleOnce(10 seconds) {
    println("About to send messages")
    client ! "ciao"
    client ! "prova"
    println("Messages sent")
  }
}