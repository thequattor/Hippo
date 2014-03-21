package unicredit.hippo
package actors

import scala.concurrent.duration._
import scala.collection.mutable.{ HashMap => MMap }

import akka.actor.Actor
import akka.util.Timeout

import storage.Repository
import messages.{ Retrieve, Result, Switch }


class Retriever(partitions: Int) extends Actor {
  val repos = MMap.empty[String, Repository]
  var version = "abcd123"

  def receive = {
    case Retrieve(table, keys, columns) =>
      val repo = repos.getOrElseUpdate(table,
        new Repository(table, partitions, version))
      val data = repo.read(keys, columns)

      sender ! Result(data)
    case Switch(v) =>
      version = v
      repos.clear
  }
}