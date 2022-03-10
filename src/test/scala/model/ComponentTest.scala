package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ComponentTest extends AnyFlatSpec with Matchers {
  import State._
  "Component" should "get ownState based on the highest of checkStates" in {
    val testInput = Component(
      id = "test",
      checkStates = Set(CheckState("CPU load", Warning), CheckState("RAM usage", Clear))
    )
    testInput.ownState mustBe Warning
  }

  "Component" should "set ownState to no_data if checkStates is empty" in {
    val testInput = Component(id = "test", checkStates = Set.empty)
    testInput.ownState mustBe NoData
  }

  "Component" should "get derivedState based on the derivedState of the components it depends on" in {
    val testDependency0 = Component(
      id = "dependency 0",
      checkStates = Set(CheckState("CPU load", Warning), CheckState("RAM usage", Alert)))
    val testDependency1 = Component(
      id = "dependency 1",
      checkStates = Set(CheckState("CPU load", NoData), CheckState("RAM usage", Warning)))
    val testDependency2 = Component(
      id = "dependency 2",
      checkStates = Set(CheckState("CPU load", Warning), CheckState("RAM usage", Clear)),
      dependsOn = Set(testDependency0))

    val testInput = Component(id = "test", checkStates = Set(CheckState("RAM usage", NoData), CheckState("CPU load", Clear)), dependsOn = Set(testDependency1, testDependency2))
    println(s"testInput.dependsOn.map(_.derivedState)= ${testInput.dependsOn.map(_.derivedState)}")
    testInput.derivedState mustBe Alert
    testInput.ownState mustBe Clear
  }

  "Component" should "get derivedState based on the higher between ownState and derivedState of the components it depends on" in {
    val testDependency0 = Component(
      id = "dependency 0",
      checkStates = Set(CheckState("CPU load", Warning), CheckState("RAM usage", Warning)))
    val testDependency1 = Component(
      id = "dependency 1",
      checkStates = Set(CheckState("CPU load", NoData), CheckState("RAM usage", Clear)))
    val testDependency2 = Component(
      id = "dependency 2",
      checkStates = Set(CheckState("CPU load", Warning), CheckState("RAM usage", Clear)),
      dependsOn = Set(testDependency0))

    val testInput = Component(id = "test",
      checkStates = Set(CheckState("CPU load", Alert)),
      dependsOn = Set(testDependency1, testDependency2)
    )
    testInput.derivedState mustBe Alert
  }

  "Component" should "ignore Clear state from components it depends on" in {
    val testDependency0 = Component(
      id = "dependency 0",
      checkStates = Set(CheckState("CPU load", Clear), CheckState("RAM usage", Clear)))
    val testDependency1 = Component(
      id = "dependency 1",
      checkStates = Set(CheckState("CPU load", NoData), CheckState("RAM usage", Clear)))
    val testDependency2 = Component(
      id = "dependency 2",
      checkStates = Set(CheckState("CPU load", NoData), CheckState("RAM usage", Clear)),
      dependsOn = Set(testDependency0))

    val testInput = Component(id = "test",
      checkStates = Set(CheckState("CPU load", NoData)),
      dependsOn = Set(testDependency1, testDependency2)
    )
    testInput.derivedState mustBe NoData
  }
}
