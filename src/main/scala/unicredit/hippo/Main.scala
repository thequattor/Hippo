package unicredit.hippo

import akka.actor.{ ActorSystem, Props }

import actors.Retriever


object Main extends App {
  // val repo = new Repository("people", 12, "abcd123")

  // def write = {
  //   repo.write("firry", Map("name" -> "Marco", "surname" -> "Firrincieli"))
  //   repo.write("ferrets", Map("name" -> "Andrea", "surname" -> "Ferretti", "age" -> "33"))
  //   repo.write("pazqo", Map("name" -> "Stefano", "surname" -> "Pascolutti"))
  // }
  val system = ActorSystem("hippo")
  val retriever = system.actorOf(Props(new Retriever(12)), name = "retriever")

  ////////////////
  import system.dispatcher
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.util.Timeout
  import messages.{ Retrieve, Result }
  implicit val timeout = Timeout(10 seconds)

  val request = Retrieve("people", List("ferrets", "pazqo"), List("name"))

  (retriever ? request).mapTo[Result] onSuccess {
    case Result(content) => println(content)
  }
  ////////////////

  readLine("Press <Enter> to shutdown...")
  system.shutdown
}