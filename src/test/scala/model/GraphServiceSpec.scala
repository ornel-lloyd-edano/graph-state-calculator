package model

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class GraphServiceSpec extends AnyFlatSpec with Matchers with ScalaFutures {
  val appComponentCyclicDependencyBreaker: Component = Component(id = "app", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)))
  val dbComponent: Component = Component(id = "db", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
    dependencyOf = Set(appComponentCyclicDependencyBreaker)) //break cyclic dependency leading to stackoverflow
  val cacheComponent: Component = Component(id = "cache", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
    dependencyOf = Set(appComponentCyclicDependencyBreaker)) //break cyclic dependency leading to stackoverflow
  val appComponent: Component = Component(id = "app", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
    dependsOn = Set(dbComponent, cacheComponent))

  val service = new GraphService {
    val storage = scala.collection.mutable.Map[String, Component](
      "app" -> appComponent,
      "db" -> dbComponent,
      "cache" -> cacheComponent)
  }

  "GraphService" should "get all components in the graph" in {
    val result = service.getTopology()
    whenReady(result) {
      case Right(result)=>
        result.components.toSet mustBe Set(appComponent, dbComponent, cacheComponent)
      case _=> fail
    }
  }

  "GraphService" should "filter components by component id" in {
    val result = service.getTopology(componentIds = Seq("db", "cache"))
    whenReady(result) {
      case Right(result)=>
        result.components.toSet mustBe Set(dbComponent, cacheComponent)
      case _=> fail
    }
  }

  "GraphService" should "create or update components in the graph" in {
    val brokerComponent = Component(id = "broker", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
      dependencyOf = Set(appComponentCyclicDependencyBreaker))
    val updatedAppComponent: Component = Component(id = "app", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
      dependsOn = Set(brokerComponent, dbComponent, cacheComponent))

    val graph = Graph(Seq(brokerComponent, updatedAppComponent))
    val result = service.createTopology(graph).flatMap(_=> service.getTopology())

    whenReady(result) {
      case Right(result)=>
        result.components.toSet mustBe Set(updatedAppComponent, brokerComponent, dbComponent, cacheComponent)
      case _=> fail
    }
  }
}
