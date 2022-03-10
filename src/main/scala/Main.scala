import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import http.Routes

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  println("starting State Calculation Server...")
  val config = ConfigFactory.load()
  implicit val system = ActorSystem()
  val routes = new Routes(model.Graph, model.Event)
  Http().newServerAt(config.getString("http.host"), config.getInt("http.port")).bind(routes.allRoutes).foreach { binding=>
    println(s"server up at ${binding.localAddress.getHostName}:${binding.localAddress.getPort}")
  }
}
