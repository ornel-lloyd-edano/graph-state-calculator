package dto

import model.CheckState

case class Component (
  id: String,
  ownState: String,
  derivedState: String,
  checkStates: Map[String, String],
  dependsOn: Option[Set[String]],
  dependencyOf: Option[Set[String]]
)

object Component extends SnakeCaseJsonProtocol {
  case class Components(components: Seq[Component])
  case class Graph(graph: Components)

  implicit val formatter = jsonFormat6(Component.apply)
  implicit val compsFormatter = jsonFormat1(Components)
  implicit val grphFormatter = jsonFormat1(Graph)

  implicit class ComponentExtension(dto: Component) {
    def toModel(graph: Graph): model.Component =
      model.Component(
        id = dto.id,
        checkStates = dto.checkStates.map {
          case (name, value) => model.CheckState(name, model.State.valueOf(value))
        }.toSet,
        dependsOn = dto.dependsOn.map(_.flatMap(id => graph.graph.components.find(_.id == id).map(_.toModel) )).getOrElse(Set.empty),
        dependencyOf = dto.dependencyOf.map(_.flatMap(id => graph.graph.components.find(_.id == id).map(_.toModel) )).getOrElse(Set.empty)
      )

    def toModel: model.Component =
      model.Component(
        id = dto.id,
        checkStates = dto.checkStates.map {
          case (name, value) => model.CheckState(name, model.State.valueOf(value))
        }.toSet
      )
  }

  implicit class GraphExtension(dto: Graph) {
    def toModel: model.Graph =
      model.Graph(dto.graph.components.map(_.toModel(dto)))
  }

  implicit class ComponentModelExtension(component: model.Component) {
    def toApi: dto.Component =
      dto.Component(
        id = component.id,
        ownState = component.ownState.value,
        derivedState = component.derivedState.value,
        checkStates = component.checkStates.map {
          case CheckState(name, value)=> name -> value.value
        }.toMap,
        dependsOn = if (component.dependsOn.nonEmpty) Option(component.dependsOn.map(_.id)) else None,
        dependencyOf = if (component.dependencyOf.nonEmpty) Option(component.dependencyOf.map(_.id)) else None
      )
  }

  implicit class GraphModelExtension(graph: model.Graph) {
    def toApi: dto.Component.Graph =
      dto.Component.Graph(
        graph = Components(graph.components.map(_.toApi))
      )
  }

}
