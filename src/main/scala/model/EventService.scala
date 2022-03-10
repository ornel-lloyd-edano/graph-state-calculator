package model

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait EventService {
  val graphService: GraphService
  val lastUpdatedAt: mutable.Map[ComponentId, Long]

  /**
   * Updates the checkStates of components in the graph based on the matching component id in the list of events
   * Ignores event if the timestamp of the event to be applied is equal or lower than the last update timestamp of that component
   * @param events list of events to update the components of the graph
   * @return
   */
  def applyEvents(events: Seq[Event]): Future[Either[Throwable, Unit]] = {
    graphService.getTopology().flatMap(_.fold(err=> Future.successful(Left(err)),
        { graph=>
          val localLastUpdatedAt = mutable.Map[ComponentId, Long]()
          val updatedComponents = recursiveApplyEvents(graph.components, events, localLastUpdatedAt)
          localLastUpdatedAt.foreach { case (componentId, timestamp)=>
            this.lastUpdatedAt.put(componentId, timestamp)
          }
          graphService.createTopology(Graph(updatedComponents)).map(_.fold(err=> Left(err), _=> Right(()) ))
        }
      ))
  }

  private def recursiveApplyEvents(components: Iterable[Component], events: Seq[Event], localLastUpdatedAt: mutable.Map[ComponentId, Long]): Seq[Component] = {
    components.foldLeft[Seq[Component]](Nil) { (updatedComponents, component)=>
      val checkStatesMap = component.checkStates.map(chkState=> chkState.name -> chkState).toMap
      val updatedChkStates = events.sorted.filter(_.component.id == component.id)
        .foldLeft(checkStatesMap) { (updatedCheckStates, event)=>
          event.component.checkStates.headOption match {
            case Some(checkState) if this.lastUpdatedAt.get(event.component.id).forall(_ < event.timestamp) ||
              this.lastUpdatedAt.get(event.component.id).isEmpty=>
                localLastUpdatedAt.put(event.component.id, event.timestamp)
                updatedCheckStates + (checkState.name -> checkState)
            case _=>
              updatedCheckStates
          }
        }
      updatedComponents :+ component.copy(checkStates = updatedChkStates.map(_._2).toSet,
        dependsOn = recursiveApplyEvents(component.dependsOn, events, localLastUpdatedAt).toSet,
        dependencyOf = recursiveApplyEvents(component.dependencyOf, events, localLastUpdatedAt).toSet)
    }
  }
}
