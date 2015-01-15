package webserviceclients.audit

import play.api.libs.json.Json._
import play.api.libs.json._

case class AuditRequest(name: String, serviceType: String, data: Seq[(String, Any)])

object AuditRequest {

  implicit val jsonWrites = new Writes[Seq[(String, Any)]] {
    def writes(o: Seq[(String, Any)]): JsValue = obj(
      o.map {
        case (key: String, value: Any) =>
          val ret: (String, JsValueWrapper) = value match {
            case asString: String => key -> JsString(asString)
            case asInt: Int => key -> JsNumber(asInt)
            case asDouble: Double => key -> JsNumber(asDouble)
            case None => key -> JsNull
            case asBool: Boolean => key -> JsBoolean(asBool)
            case _ => throw new RuntimeException("no match, you need to tell it how to cast this type to json")
          }
          ret
      }.toSeq: _*
    )
  }

  implicit val auditMessageFormat = Json.writes[AuditRequest]
}