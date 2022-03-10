package dto

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, JsonFormat}

case class Event (timestamp: Long, component: String, checkState: String, state: String)

object Event extends DefaultJsonProtocol {
  case class Events(events: Seq[Event])

  implicit object formatter extends JsonFormat[Event] {
    override def write(obj: Event): JsValue =
      JsObject(
        "timestamp" -> JsString(obj.timestamp.toString),
        "component" -> JsString(obj.component),
        "check_state" -> JsString(obj.checkState),
        "state" -> JsString(obj.state)
      )

    override def read(json: JsValue): Event = {
      val fields = json.asJsObject.fields
      val timestamp = fields.get("timestamp") match {
        case Some(JsString(value))=> value.toLong
        case Some(JsNumber(value))=> value.toLong
        case _=> throw DeserializationException("Fail to read Event timestamp")
      }
      val component = fields.get("component") match {
        case Some(JsString(value))=> value
        case _=> throw DeserializationException("Fail to read Event component")
      }
      val checkState = fields.get("check_state") match {
        case Some(JsString(value))=> value
        case _=> throw DeserializationException("Fail to read Event check_state")
      }
      val state = fields.get("state") match {
        case Some(JsString(value))=> value
        case _=> throw DeserializationException("Fail to read Event state")
      }
      Event(timestamp, component, checkState, state)
    }
  }
  implicit val evtsFormatter = jsonFormat1(Events)

  implicit class EventExtension(dto: Event) {
    def toModel: model.Event = {
      model.Event(
        timestamp = dto.timestamp,
        component = model.Component(
          id = dto.component,
          checkStates = Set(model.CheckState(name = dto.checkState, value = model.State.valueOf(dto.state)))
        )
      )
    }
  }

}
