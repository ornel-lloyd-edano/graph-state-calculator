package dto

import dto.Component.{Components, Graph}
import dto.Event.Events
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import spray.json._

class JsonFormatTest extends AnyFlatSpec with Matchers {

  "Graph" should "deserialize" in {
    val graphJson =
      """
      |{
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

    val expected = Graph(Components(Seq(
      Component(
        id = "app", ownState = "no_data", derivedState = "no_data",
        checkStates = Map("CPU load" -> "no_data", "RAM usage" -> "no_data"),
        dependsOn = Option(Set("db")), dependencyOf = None),
      Component(
        id = "db", ownState = "no_data", derivedState = "no_data",
        checkStates = Map("CPU load" -> "no_data", "RAM usage" -> "no_data"),
        dependsOn = None, dependencyOf = Option(Set("app")))
    )))

    graphJson.convertTo[Graph] mustBe expected
  }

  "Graph" should "serialize" in {
    val graph = Graph(Components(Seq(
      Component(
        id = "app", ownState = "no_data", derivedState = "no_data",
        checkStates = Map("CPU load" -> "no_data", "RAM usage" -> "no_data"),
        dependsOn = Option(Set("db")), dependencyOf = None),
      Component(
        id = "db", ownState = "no_data", derivedState = "no_data",
        checkStates = Map("CPU load" -> "no_data", "RAM usage" -> "no_data"),
        dependsOn = None, dependencyOf = Option(Set("app")))
    )))

    val expected =
    """
       |{
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

    graph.toJson mustBe expected
  }

  "Event" should "deserialize" in {
    val eventJson =
      """
        |{
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
        |""".stripMargin.parseJson

    val expected = Events(Seq(
      Event(1, "db", "CPU load", "warning"),
      Event(2, "app", "CPU load", "clear")
    ))

    eventJson.convertTo[Events] mustBe expected
  }

  "Event" should "serialize" in {
    val events = Events(Seq(
      Event(1, "db", "CPU load", "warning"),
      Event(2, "app", "CPU load", "clear")
    ))

    val expected =
      """
        |{
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
        |""".stripMargin.parseJson

    events.toJson mustBe expected
  }
}
