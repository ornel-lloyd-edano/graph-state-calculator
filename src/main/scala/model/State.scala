package model

sealed abstract class State(val value: String, val level: Int) extends Ordered[State] {
  override def compare(that: State): Int = this.level - that.level
}

/**
 * Each of the states has one of the following values, ordered from low to high:
 * no_data - no state is known
 * clear - the component is running fine
 * warning - something is wrong
 * alert - something went wrong
 */
object State {
  case object NoData extends State("no_data", 0)
  case object Clear extends State("clear", 1)
  case object Warning extends State("warning", 2)
  case object Alert extends State("alert", 3)

  def valueOf(arg: String): State = arg.trim.toLowerCase match {
    case "no_data" => NoData
    case "clear"   => Clear
    case "warning" => Warning
    case "alert"   => Alert
  }

  implicit class IterableStateExtension(arg: Iterable[State]) {
    def getHighest: Option[State] = arg.toSeq.sorted.lastOption
  }
}
