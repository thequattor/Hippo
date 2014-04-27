package unicredit.hippo

import scala.concurrent.Future

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
  case object GetSiblings
  case class Siblings(nodes: Map[String, ActorRef])
  case object RefreshNodes
  case object AreYouReady
  case class ReadyState(state: Future[Boolean])
  case class Download(source: String, target: String, shard: String)
}