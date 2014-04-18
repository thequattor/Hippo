package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.collection.mutable.{ HashMap => MMap }

import akka.actor.{ Actor, ActorLogging }
import akka.util.Timeout

import storage.Repository
import messages.{ Retrieve, Result, Switch }


class Retriever(home: String) extends Actor with ActorLogging {
  val repos = MMap.empty[String, Repository]
  var version = "abcd123"

  def receive = {
    case Retrieve(table, keys, columns) ⇒
      val repo = repos.getOrElseUpdate(table,
        new Repository(home, table, version))
      val data = repo.read(keys, columns)

      sender ! Result(data)
    case Switch(v) ⇒
      log.info(s"Switching to version $v")
      version = v
      repos.clear
  }
}