package audit

import java.util.UUID
import models.{BusinessDetailsModel, PaymentModel, VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import controllers.Payment

//
// base classes
//
// TODO remove this class and use IEP's version when jar becomes available
case class Message(name: String, serviceType: String, data: (String, Any)*) {

  var messageId = UUID.randomUUID

  def getDataAsJava: java.util.Map[String, Any] = {
    import scala.collection.JavaConverters._
    data.toMap.asJava
  }
}

object Message {

  private implicit val dataJsonWrites = new Writes[Seq[(String, Any)]] {
    def writes(data: Seq[(String, Any)]): JsValue = {
      val mapOfStrings: Map[String, String] = data.map(x => (x._1, x._2.toString)).toMap
      toJson(mapOfStrings)
    }
  }

  implicit val JsonWrites = new Writes[Message] {
    def writes(cache: Message): JsValue = {
      Json.obj(
        "name" -> toJson(cache.name),
        "serviceType" -> toJson(cache.serviceType),
        "data" -> toJson(cache.data)
      )
    }
  }
}

object Timestamp {

  private val dateService = new DateServiceImpl

  def getTimestamp = {
    val timestamp = dateService.today.toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(timestamp) + " " +
      ISODateTimeFormat.hourMinuteSecondMillis().print(timestamp)
    s"$isoDateTimeString:${timestamp.getZone}"
  }
}

object KeeperNameOptString {

  def from(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel) = {

    // flatten and then iterate
    val keeperNameList = List(vehicleAndKeeperDetailsModel.title,
      vehicleAndKeeperDetailsModel.firstName,
      vehicleAndKeeperDetailsModel.lastName).flatten

    if (keeperNameList.size > 0) {
      var nameString = keeperNameList(0)
      for (nameItem <- keeperNameList.drop(1)) {
        nameString += (" " + nameItem)
      }
      Some(nameString)
    } else {
      None
    }
  }
}

object KeeperAddressOptString {

  def from(addressModel: Option[AddressModel]) = {

    addressModel match {
      case Some(address) =>
        if (address.address.size > 0) {
          var addressString = address.address(0)
          for (addressLine <- address.address.drop(1)) {
            addressString += (", " + addressLine)
          }
          Some(addressString)
        } else {
          None
        }
      case _ =>
        None
    }
  }
}

object BusinessAddressOptString {

  def from(businessDetailsModel: BusinessDetailsModel) = {

    var addressString = businessDetailsModel.name

    if (businessDetailsModel.address.address.size > 0) {
      for (addressLine <- businessDetailsModel.address.address) {
        addressString += (", " + addressLine)
      }
    }
    Some(addressString)
  }
}

object BusinessDetailsModelOptSeq {

  def from(businessDetailsModel: Option[BusinessDetailsModel]) = {
    businessDetailsModel match {
      case Some(businessDetailsModel) => {
        val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
        val businessAddressOpt = BusinessAddressOptString.from(businessDetailsModel).map(
          businessAddress => ("businessAddress", businessAddress))
        val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
        Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
      }
      case _ => Seq.empty
    }
  }
}

object VehicleAndKeeperDetailsModelOptSeq {

  def from(vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel]) = {
    vehicleAndKeeperDetailsModel match {
      case Some(vehicleAndKeeperDetailsModel) => {
        val currentVrmOpt = Some(("currentVrm", vehicleAndKeeperDetailsModel.registrationNumber))
        val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
        val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
        val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
          keeperName => ("keeperName", keeperName))
        val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
          keeperAddress => ("keeperAddress", keeperAddress))
        Seq(currentVrmOpt, makeOpt, modelOpt, keeperNameOpt, keeperAddressOpt)
      }
      case _ => Seq.empty
    }
  }
}

object PaymentModelOptSeq {

  def from(paymentModelOpt: Option[PaymentModel], paymentStatus: Option[String]) = {
    paymentModelOpt match {
      case Some(paymentModel) => {
        val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
        val paymentStatusOpt = paymentStatus.map(paymentStatus => ("paymentStatus", paymentStatus))
        val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
        val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
        val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
        val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
        val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
        val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(
          totalAmountPaid => ("paymentTotalAmountPaid", totalAmountPaid / 100.0))
        Seq(paymentTrxRefOpt, paymentStatusOpt, paymentMaskedPanOpt, paymentAuthCodeOpt, paymentMerchantIdOpt,
          paymentTypeOpt, paymentCardTypeOpt, paymentTotalAmountPaidOpt)
      }
      case _ => Seq.empty
    }
  }
}

object AuditMessage {

  // service types
  private final val PersonalisedRegServiceType = "PR Retention"

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


  def from(pageMovement: String,
           transactionId: String,
           vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel] = None,
           replacementVrm: Option[String] = None,
           keeperEmail: Option[String] = None,
           businessDetailsModel: Option[BusinessDetailsModel] = None,
           paymentModel: Option[PaymentModel] = None,
           retentionCertId: Option[String] = None,
           rejectionCode: Option[String] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val vehicleAndKeeperDetailsModelOptSeq = VehicleAndKeeperDetailsModelOptSeq.from(vehicleAndKeeperDetailsModel)
      val replacementVRMOpt = replacementVrm.map(replacementVrm => ("replacementVRM", replacementVrm))
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModel)
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModel, Some(Payment.SettledStatus))
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
    Message(pageMovement, PersonalisedRegServiceType, data: _*)
  }
}