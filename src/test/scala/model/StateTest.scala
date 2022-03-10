package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class StateTest extends AnyFlatSpec with Matchers {
  import State._
  "State" should "be sortable" in {
    val testInput = Seq(NoData, Alert, Clear, NoData, Warning, NoData, Clear, Alert, Clear, NoData)
    val expected = Seq(NoData, NoData, NoData, NoData, Clear, Clear, Clear, Warning, Alert, Alert)
    testInput.sorted mustBe expected
  }

  "Collection of State" should "yield the highest State" in {
    val testInput = Seq(NoData, Clear, Clear, NoData, Alert, NoData, Clear, Warning, Clear, Warning)
    testInput.getHighest mustBe Some(Alert)
  }
}
