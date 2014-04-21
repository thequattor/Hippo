package unicredit.hippo

import scala.collection.JavaConversions._

import com.typesafe.config.ConfigFactory

import messages._


object Main extends App {
  private val config = ConfigFactory.load
  private val contacts = config.getStringList("hippo.seed-nodes")

  val client = new HippoClient("localhost", 2551)
  val message = Request(
    table = "wiki_it_index",
    keys = List("Alerione", "Antiedizioni", "Afroasiatico"),
    columns = List("count", "documents")
  )

  client.query(message, { case r => println(r) })
}