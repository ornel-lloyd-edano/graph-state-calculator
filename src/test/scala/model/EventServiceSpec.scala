package model

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class EventServiceSpec extends AnyFlatSpec with Matchers with ScalaFutures {

  val appComponentCyclicDependencyBreaker: Component = Component(id = "app", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)))
  val dbComponent: Component = Component(id = "db", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
    dependencyOf = Set(appComponentCyclicDependencyBreaker)) //break cyclic dependency leading to stackoverflow
  val cacheComponent: Component = Component(id = "cache", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
    dependencyOf = Set(appComponentCyclicDependencyBreaker)) //break cyclic dependency leading to stackoverflow
  val appComponent: Component = Component(id = "app", checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.NoData)),
    dependsOn = Set(dbComponent, cacheComponent))

  val mockGraphService = new GraphService {
    val storage = scala.collection.mutable.Map[String, Component](
      "app" -> appComponent,
      "db" -> dbComponent,
      "cache" -> cacheComponent)
  }
  val service = new EventService {
    override val graphService: GraphService = mockGraphService
    override val lastUpdatedAt: mutable.Map[ComponentId, Long] = mutable.Map()
  }

  "EventService" should "update the components in the graph by taking in some events" in {
    val events = Seq(
      Event(2, Component("app", checkStates = Set(CheckState("RAM usage", State.Warning)))),
      Event(4, Component("db", checkStates = Set(CheckState("Disk usage", State.Warning)))),
      Event(3, Component("app", checkStates = Set(CheckState("CPU load", State.Clear)))),
      Event(1, Component("app", checkStates = Set(CheckState("CPU load", State.Alert)))),
      Event(5, Component("cache", checkStates = Set(CheckState("RAM usage", State.Alert)))),
      Event(7, Component("db", checkStates = Set(CheckState("RAM usage", State.Warning)))),
      Event(6, Component("db", checkStates = Set(CheckState("Disk usage", State.Alert))))
    )
    val result = service.applyEvents(events).flatMap(_=> mockGraphService.getTopology())
    val updatedAppComponent = appComponent.copy(checkStates = Set(CheckState("CPU load", State.Clear), CheckState("RAM usage", State.Warning)))
    val updatedCacheComponent = cacheComponent.copy(checkStates = Set(CheckState("CPU load", State.NoData), CheckState("RAM usage", State.Alert)))
    val updatedDbComponent = dbComponent.copy(checkStates = Set(CheckState("CPU load", State.NoData), CheckState("Disk usage", State.Alert), CheckState("RAM usage", State.Warning)))
    whenReady(result) {
      case Right(result)=>
        result.components.map(_.copy(dependsOn = Set.empty, dependencyOf = Set.empty)).toSet mustBe Set(updatedAppComponent, updatedCacheComponent, updatedDbComponent).map(_.copy(dependsOn = Set.empty, dependencyOf = Set.empty))
      case _=> fail
    }
  }

  "EventService" should "ignore events if the timestamp is lower than or equal to the last update timestamp of that component" in {
    val events = Seq(
      Event(1, Component("app", checkStates = Set(CheckState("CPU load", State.Alert)))),
      Event(1, Component("cache", checkStates = Set(CheckState("RAM usage", State.Clear)))),
      Event(1, Component("db", checkStates = Set(CheckState("RAM usage", State.Clear))))
    )
    val result = service.applyEvents(events).flatMap(_=> mockGraphService.getTopology())
    whenReady(result) {
      case Right(result)=>
        val checkStates = result.components.flatMap(comp=> comp.checkStates.map(chkState=> (comp.id, chkState)))
        checkStates.contains( ("app", CheckState("CPU load", State.Alert)) ) mustBe false
        checkStates.contains( ("cache", CheckState("RAM usage", State.Clear)) ) mustBe false
        checkStates.contains( ("db", CheckState("RAM usage", State.Clear)) ) mustBe false
      case _=> fail
    }
  }

}
