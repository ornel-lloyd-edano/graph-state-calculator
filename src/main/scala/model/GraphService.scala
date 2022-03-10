package model

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait GraphService {
  val storage: mutable.Map[ComponentId, Component]

  /**
   * Get the entire graph of components from the storage with the option to pick only certain components
   * @param componentIds list of component ids to filter, empty means fetch all components in the graph
   * @return Graph
   */
  def getTopology(componentIds: Seq[String] = Nil): Future[Either[Throwable, Graph]] = Future {
    Try {
      val components = if (componentIds.nonEmpty) {
        storage.collect { case (id, component) if componentIds.contains(id) =>
          component
        }
      } else {
        storage.map(_._2)
      }
      Graph(components.toSeq.sortBy(_.id))
    }.toEither
  }

  /**
   * Create the entire graph of components and updates existing components in the storage
   * @param graph the full graph of components to save
   * @return Unit
   */
  def createTopology(graph: Graph): Future[Either[Throwable, Unit]] = Future {
    Try {
      graph.components.foreach { component=>
        storage.put(component.id, component)
      }
    }.toEither
  }
}
