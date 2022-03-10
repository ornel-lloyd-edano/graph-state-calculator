package model

import State._

case class Component private (
  id: String,
  checkStates: Set[CheckState],
  dependsOn: Set[Component],
  dependencyOf: Set[Component]
) {

  /***
   *The own state, singular, is the highest/most severe state of the component's check states.
   * It is not affected by anything other then the check states.
   * If the component has no check states the own state is no_data
   *  @return State
   */
  def ownState: State = checkStates.map(_.value).getHighest.getOrElse(State.NoData)

  /**
   * The derived state, singular, is the higher of
   * A) The own state (warning and higher) or
   * B) the highest of the derived states of the components it depends on
   * `Clear` state does not propagate, so if the own state of a component is `clear` or `no_data`
   * and its dependent derived states are `clear` or `no_data`, then the derived state is set to `no_data`
   * @return State
   */
  def derivedState: State = (dependsOn.map(_.derivedState).toSeq :+ ownState)
    .filterNot(_ == State.Clear).getHighest.getOrElse(State.NoData)
}

object Component {
  def apply(
    id: String,
    checkStates: Set[CheckState] = Set.empty,
    dependsOn: Set[Component] = Set.empty,
    dependencyOf: Set[Component] = Set.empty): Component =
    new Component(id, checkStates, dependsOn, dependencyOf)

}
