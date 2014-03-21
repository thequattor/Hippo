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
  val retriever = system.actorOf(Props[Retriever], name = "retriever")


  readLine("Press <Enter> to shutdown...")
  system.shutdown
}