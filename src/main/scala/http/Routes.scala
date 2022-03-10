package http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import model.{EventService, GraphService}
import spray.json.enrichAny

import scala.util.{Failure, Success}

class Routes(val graphService: GraphService, val eventsService: EventService) extends SprayJsonSupport {
  import dto.Component._
  import dto.Event._

  def getGraph: Route =
    path("api" / "topology") {
      get {
        onComplete(graphService.getTopology()) {
          case Success(Right(result))=>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, result.toApi.toJson.prettyPrint))
          case Success(Left(error))=>
            complete(StatusCodes.BadRequest, error.getMessage)
          case Failure(exception)=>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }

  def createGraph: Route =
    path("api" / "topology") {
      post {
        entity(as[Graph]) { graph=>
          onComplete(graphService.createTopology(graph.toModel)) {
            case Success(Right(_))=>
              complete(StatusCodes.Created, "Graph created successfully")
            case Success(Left(error))=>
              complete(StatusCodes.BadRequest, error.getMessage)
            case Failure(exception)=>
              complete(StatusCodes.InternalServerError, exception.getMessage)
          }
        }
      }
    }

  def consumeEvents: Route =
    path("api" / "events") {
      post {
        entity(as[Events]) { case Events(events)=>
          onComplete(eventsService.applyEvents(events.map(_.toModel))) {
            case Success(Right(_))=>
              complete(StatusCodes.OK, "Events applied to topology successfully")
            case Success(Left(error))=>
              complete(StatusCodes.BadRequest, error.getMessage)
            case Failure(exception)=>
              complete(StatusCodes.InternalServerError, exception.getMessage)
          }
        }
      }
    }

  val allRoutes = getGraph ~ createGraph ~ consumeEvents
}
