package unicredit.hippo

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import actors.Client
import messages._


// This client is meant to be used from Scala
// applications that are not based on Akka. It
// starts its own actor system.
class HippoClient(host: String, port: Int) {
  private val contact = s"akka.tcp://hippo@$host:$port"
  private val system = ActorSystem("hippo-client")
  private implicit val timeout = Timeout(5 seconds)
  private val client = system.actorOf(
    Props(new Client(List(contact))),
    name = "client"
  )
  import system.dispatcher

  // Since the client will be used outside of Akka,
  // users may not have a way to wait for the client
  // to be ready (asynchronously). We do the easy
  // thing and just block until the client has
  // established connection to the cluster.
  private val ready = Await.result((client ? AreYouReady).mapTo[ReadyState], 5 seconds)
  Await.ready(ready.state, 5 seconds)

  def query(request: Request, callback: Result ⇒ Unit) =
    (client ? request).mapTo[Result] onSuccess PartialFunction(callback)
  def siblings(callback: Siblings ⇒ Unit) =
    (client ? GetSiblings).mapTo[Siblings] onSuccess PartialFunction(callback)
  def close = system.shutdown
}