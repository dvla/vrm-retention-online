package webserviceclients.audit2

import models.BusinessDetailsModel
import models.PaymentModel
import play.api.libs.json.Json._
import play.api.libs.json._
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

case class AuditRequest(name: String, serviceType: String, data: Seq[(String, Any)])

object AuditRequest {

  // service types
  final val PersonalisedRegServiceType = "PR Retention"

  // page movement names
  final val VehicleLookupToConfirm = "VehicleLookupToConfirm"
  final val VehicleLookupToConfirmBusiness = "VehicleLookupToConfirmBusiness"
  final val VehicleLookupToCaptureActor = "VehicleLookupToCaptureActor"
  final val VehicleLookupToVehicleLookupFailure = "VehicleLookupToVehicleLookupFailure"
  final val VehicleLookupToExit = "VehicleLookupToExit"
  final val VehicleLookupToMicroServiceError = "VehicleLookupToMicroServiceError"
  final val CaptureActorToConfirmBusiness = "CaptureActorToConfirmBusiness"
  final val CaptureActorToExit = "CaptureActorToExit"
  final val ConfirmBusinessToConfirm = "ConfirmBusinessToConfirm"
  final val ConfirmBusinessToExit = "ConfirmBusinessToExit"
  final val ConfirmToPayment = "ConfirmToPayment"
  final val ConfirmToExit = "ConfirmToExit"
  final val PaymentToSuccess = "PaymentToSuccess"
  final val PaymentToPaymentNotAuthorised = "PaymentToPaymentNotAuthorised"
  final val PaymentToPaymentFailure = "PaymentToPaymentFailure"
  final val PaymentToExit = "PaymentToExit"
  final val PaymentToMicroServiceError = "PaymentToMicroServiceError"

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

  def from(pageMovement: String,
           transactionId: String,
           timestamp: String,
           vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel] = None,
           replacementVrm: Option[String] = None,
           keeperEmail: Option[String] = None,
           businessDetailsModel: Option[BusinessDetailsModel] = None,
           paymentModel: Option[PaymentModel] = None,
           retentionCertId: Option[String] = None,
           rejectionCode: Option[String] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", timestamp))
      val vehicleAndKeeperDetailsModelOptSeq = VehicleAndKeeperDetailsModelOptSeq.from(vehicleAndKeeperDetailsModel)
      val replacementVRMOpt = replacementVrm.map(replacementVrm => ("replacementVrm", replacementVrm))
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModel)
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModel)
      val retentionCertIdOpt = retentionCertId.map(retentionCertId => ("retentionCertId", retentionCertId))
      val rejectionCodeOpt = rejectionCode.map(rejectionCode => ("rejectionCode", rejectionCode))

      (Seq(
        transactionIdOpt,
        timestampOpt,
        replacementVRMOpt,
        keeperEmailOpt,
        retentionCertIdOpt,
        rejectionCodeOpt
      ) ++ vehicleAndKeeperDetailsModelOptSeq ++ businessDetailsModelOptSeq ++ paymentModelOptSeq).flatten
    }
    AuditRequest(pageMovement, PersonalisedRegServiceType, data)
  }
}