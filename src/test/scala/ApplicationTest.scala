import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import http.Routes
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import spray.json._

import scala.concurrent.duration._

class ApplicationTest extends AnyFlatSpec with Matchers with ScalatestRouteTest  with SprayJsonSupport {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  val routes = new Routes(model.Graph, model.Event)

  "Application" should "initially get an empty graph" in {
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

  "Application" should "create the given graph" in {
    val requestBody =
      """{
        |  "graph": {
        |    "components": [
        |      {
        |        "id": "app",
        |        "own_state": "no_data",
        |        "derived_state": "no_data",
        |        "check_states": {
        |          "CPU load": "no_data",
        |          "RAM usage": "no_data"
        |        },
        |        "depends_on": ["db"]
        |      },
        |      {
        |        "id": "db",
        |        "own_state": "no_data",
        |        "derived_state": "no_data",
        |        "check_states": {
        |          "CPU load": "no_data",
        |          "RAM usage": "no_data"
        |        },
        |        "dependency_of": ["app"]
        |      }
        |    ]
        |  }
        |}
        |""".stripMargin
    Post("/api/topology").withEntity(ContentTypes.`application/json`, requestBody ) ~> routes.createGraph ~> check {
      status mustBe StatusCodes.Created
      val expected = "Graph created successfully"
      entityAs[String] mustBe expected
    }
  }

  "Application" should "be able to get the updated graph" in {
    Get("/api/topology") ~> routes.getGraph ~> check {
      status mustBe StatusCodes.OK
      val expected =
        """{
          |  "graph": {
          |    "components": [
          |      {
          |        "id": "app",
          |        "own_state": "no_data",
          |        "derived_state": "no_data",
          |        "check_states": {
          |          "CPU load": "no_data",
          |          "RAM usage": "no_data"
          |        },
          |        "depends_on": ["db"]
          |      },
          |      {
          |        "id": "db",
          |        "own_state": "no_data",
          |        "derived_state": "no_data",
          |        "check_states": {
          |          "CPU load": "no_data",
          |          "RAM usage": "no_data"
          |        },
          |        "dependency_of": ["app"]
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin.parseJson
      entityAs[JsValue] mustBe expected
    }
  }

  "Application" should "be able to take in some events and update the graph" in {
    val requestBody =
      """{
        |  "events": [
        |    {
        |      "timestamp": "1",
        |      "component": "db",
        |      "check_state": "CPU load",
        |      "state": "warning"
        |    },
        |    {
        |      "timestamp": "2",
        |      "component": "app",
        |      "check_state": "CPU load",
        |      "state": "clear"
        |    }
        |  ]
        |}
        |""".stripMargin
    Post("/api/events").withEntity(ContentTypes.`application/json`, requestBody ) ~> routes.consumeEvents ~> check {
      status mustBe StatusCodes.OK
      val expected = "Events applied to topology successfully"
      entityAs[String] mustBe expected
    }
  }

  "Application" should "be able to get the updated graph based from the applied events" in {
    Get("/api/topology") ~> routes.getGraph ~> check {
      status mustBe StatusCodes.OK
      val expected =
        """{
          |  "graph": {
          |    "components": [
          |      {
          |        "id": "app",
          |        "own_state": "clear",
          |        "derived_state": "warning",
          |        "check_states": {
          |          "CPU load": "clear",
          |          "RAM usage": "no_data"
          |        },
          |        "depends_on": ["db"]
          |      },
          |      {
          |        "id": "db",
          |        "own_state": "warning",
          |        "derived_state": "warning",
          |        "check_states": {
          |          "CPU load": "warning",
          |          "RAM usage": "no_data"
          |        },
          |        "dependency_of": ["app"]
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin.parseJson
      entityAs[JsValue] mustBe expected
    }
  }

  "Application" should "ignore events with outdated timestamps" in {
    val requestBody =
      """{
        |  "events": [
        |    {
        |      "timestamp": "1",
        |      "component": "db",
        |      "check_state": "CPU load",
        |      "state": "alert"
        |    },
        |    {
        |      "timestamp": "2",
        |      "component": "app",
        |      "check_state": "CPU load",
        |      "state": "alert"
        |    }
        |  ]
        |}
        |""".stripMargin
    Post("/api/events").withEntity(ContentTypes.`application/json`, requestBody ) ~> routes.consumeEvents ~> check {
      status mustBe StatusCodes.OK
      val expected = "Events applied to topology successfully"
      entityAs[String] mustBe expected
    }
  }

  "Application" should "confirm if graph was not affected with outdated events" in {
    Get("/api/topology") ~> routes.getGraph ~> check {
      status mustBe StatusCodes.OK
      val expected =
        """{
          |  "graph": {
          |    "components": [
          |      {
          |        "id": "app",
          |        "own_state": "clear",
          |        "derived_state": "warning",
          |        "check_states": {
          |          "CPU load": "clear",
          |          "RAM usage": "no_data"
          |        },
          |        "depends_on": ["db"]
          |      },
          |      {
          |        "id": "db",
          |        "own_state": "warning",
          |        "derived_state": "warning",
          |        "check_states": {
          |          "CPU load": "warning",
          |          "RAM usage": "no_data"
          |        },
          |        "dependency_of": ["app"]
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin.parseJson
      entityAs[JsValue] mustBe expected
    }
  }

}
