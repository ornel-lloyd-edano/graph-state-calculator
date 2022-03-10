package model

case class Graph (components: Seq[Component])

object Graph extends GraphService {
  val storage = scala.collection.mutable.Map[ComponentId, Component]()
}
