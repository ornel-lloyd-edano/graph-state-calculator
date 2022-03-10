package http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import spray.json._
import model.{EventService, GraphService}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.Future
import scala.concurrent.duration._

class RoutesSpec extends AnyFlatSpec with Matchers with MockFactory with ScalatestRouteTest  with SprayJsonSupport {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  val mockGraphService = mock[GraphService]
  val mockEventService = mock[EventService]
  val routes = new Routes(mockGraphService, mockEventService)

  "Routes" should "get a graph" in {
    (mockGraphService.getTopology _).expects(Nil).returning(Future.successful(Right(model.Graph(Nil))))
    Get("/api/topology") ~> routes.getGraph ~> check {
      status mustBe StatusCodes.OK
      val expected =
        s"""{
           |  "graph": {
           |    "components": [ ]
           |  }
           |}
           |""".stripMargin.parseJson
      entityAs[JsValue] mustBe expected
    }
  }

  "Routes" should "create a graph" in {
    (mockGraphService.createTopology _).expects(model.Graph(Nil)).returning(Future.successful(Right(())))
    val requestBody = dto.Component.Graph(dto.Component.Components(Nil)).toJson.prettyPrint
    Post("/api/topology").withEntity(ContentTypes.`application/json`, requestBody ) ~> routes.createGraph ~> check {
      status mustBe StatusCodes.Created
      val expected = "Graph created successfully"
      entityAs[String] mustBe expected
    }
  }

  "Routes" should "receive events" in {
    (mockEventService.applyEvents _).expects(Nil).returning(Future.successful(Right(())))
    val requestBody = dto.Event.Events(Nil).toJson.prettyPrint
    Post("/api/events").withEntity(ContentTypes.`application/json`, requestBody ) ~> routes.consumeEvents ~> check {
      status mustBe StatusCodes.OK
      val expected = "Events applied to topology successfully"
      entityAs[String] mustBe expected
    }
  }
}
