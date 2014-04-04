package unicredit.hippo

import akka.actor.ActorRef


package object messages {
  case class IdentifyTo(who: ActorRef)
  case class MyIdIs(who: ActorRef, id: String)
  case class Request(
    table: String,
    keys: List[String],
    columns: List[String]
  )
  case class Retrieve(
    table: String,
    keys: List[String],
    columns: List[String]
  )
  case class Result(content: Map[String, Map[String, String]])
  case class Switch(version: String)
}