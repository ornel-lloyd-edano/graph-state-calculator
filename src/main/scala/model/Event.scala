package model

import scala.collection.mutable

case class Event (timestamp: Long, component: Component) extends Ordered[Event] {
  override def compare(that: Event): Int = this.timestamp.toInt - that.timestamp.toInt
}

object Event extends EventService {
  val graphService: GraphService = Graph
  val lastUpdatedAt: mutable.Map[ComponentId, Long] = mutable.Map()
}
