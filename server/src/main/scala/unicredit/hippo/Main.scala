package unicredit.hippo

import akka.actor.{ ActorSystem, Props }
import akka.contrib.pattern.ClusterReceptionistExtension
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._

import actors.{ Retriever, Frontend }


object Main extends App {
  private val config = ConfigFactory.load
  private val home = config.as[String]("storage.home")

  implicit val system = ActorSystem("hippo")
  val retriever = system.actorOf(
    Props(new Retriever(home)),
    name = "retriever"
  )
  val frontend = system.actorOf(
    Props(new Frontend(retriever)),
    name = "frontend"
  )

  ClusterReceptionistExtension(system).registerService(frontend)
}