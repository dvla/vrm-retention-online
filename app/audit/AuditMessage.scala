package audit

import java.util.UUID
import models.{BusinessDetailsModel, PaymentModel, VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import mappings.common.ErrorCodes
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

//
// audit helpers
//

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

  def from(businessDetailsModelOpt: Option[BusinessDetailsModel]) = {
    businessDetailsModelOpt match {
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


//
// concrete classes
//

object AuditMessage { // TODO work in progress to refactoring audting

  // service types
  private final val PersonalisedRegServiceType = "PR Retention"

  // page movement names
  private final val PayemntToSuccess = "PaymentToSuccess"


  def from(name: String, transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String],
           businessDetailsModelOpt: Option[BusinessDetailsModel] = None, paymentModelOpt: Option[PaymentModel],
           retentionCertId: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val retentionCertIdOpt = Some(("retentionCertId", retentionCertId))

      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModelOpt, Some(Payment.SettledStatus))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt,
        retentionCertIdOpt
      ) ++ paymentModelOptSeq ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message(name, PersonalisedRegServiceType, data: _*)
  }
}




// TODO massive amount of duplication in all the below objects, needs refactoring

object VehicleLookupToConfirmAuditMessage {

  def from(transactionId: String,
           vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, businessDetailsModelOpt: Option[BusinessDetailsModel] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("VehicleLookupToConfirm", "PR Retention", data: _*)
  }
}

object VehicleLookupToConfirmBusinessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, businessDetailsModel: BusinessDetailsModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(Some(businessDetailsModel))

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("VehicleLookupToConfirmBusiness", "PR Retention", data: _*)
  }
}

object VehicleLookupToCaptureActorAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToCaptureActor", "PR Retention", data: _*)
  }
}

object VehicleLookupToVehicleLookupFailureAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           rejectionCode: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val rejectionCodeOpt = Some(("rejectionCode", rejectionCode))

      Seq(
        transactionIdOpt,
        timestampOpt,
        currentVrmOpt,
        rejectionCodeOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToVehicleLookupFailure", "PR Retention", data: _*)
  }
}

object VehicleLookupToExitAuditMessage {

  def from(transactionId: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))

      Seq(
        transactionIdOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToExit", "PR Retention", data: _*)
  }
}

object VehicleLookupToMicroServiceErrorAuditMessage {

  def from(transactionId: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val rejectionCodeOpt = Some(("rejectionCode", ErrorCodes.MicroServiceErrorCode + " - microservice_error"))

      Seq(
        transactionIdOpt,
        rejectionCodeOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToMicroServiceError", "PR Retention", data: _*)
  }
}

object CaptureActorToConfirmBusinessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, businessDetailsModel: BusinessDetailsModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(Some(businessDetailsModel))

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("CaptureActorToConfirmBusiness", "PR Retention", data: _*)
  }
}

object CaptureActorToExitAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list
    }
    Message("CaptureActorToExit", "PR Retention", data: _*)
  }
}

object ConfirmBusinessToConfirmAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, replacementVRM: String,
           businessDetailsModel: BusinessDetailsModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(Some(businessDetailsModel))

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("ConfirmBusinessToConfirm", "PR Retention", data: _*)
  }
}

object ConfirmBusinessToExitAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], businessDetailsModelOpt: Option[BusinessDetailsModel] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("ConfirmBusinessToExit", "PR Retention", data: _*)
  }
}

object ConfirmToPaymentAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String],
           businessDetailsModelOpt: Option[BusinessDetailsModel] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("ConfirmToPayment", "PR Retention", data: _*)
  }
}

object ConfirmToExitAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], businessDetailsModelOpt: Option[BusinessDetailsModel] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("ConfirmToExit", "PR Retention", data: _*)
  }
}

object PaymentToSuccessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String],
           businessDetailsModelOpt: Option[BusinessDetailsModel] = None, paymentModelOpt: Option[PaymentModel],
           retentionCertId: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val retentionCertIdOpt = Some(("retentionCertId", retentionCertId))

      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModelOpt, Some(Payment.SettledStatus))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt,
        retentionCertIdOpt
      ) ++ paymentModelOptSeq ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("PaymentToSuccess", "PR Retention", data: _*)
  }
}

object PaymentToPaymentNotAuthorisedAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], businessDetailsModelOpt: Option[BusinessDetailsModel] = None,
           paymentModelOpt: Option[PaymentModel]) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModelOpt, None)

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt
      ) ++ paymentModelOptSeq ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("PaymentToPaymentNotAuthorised", "PR Retention", data: _*)
  }
}

object PaymentToPaymentFailureAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], businessDetailsModelOpt: Option[BusinessDetailsModel] = None,
           paymentModelOpt: Option[PaymentModel], rejectionCode: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      val rejectionCodeOpt = Some(("rejectionCode", rejectionCode))

      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModelOpt, None)

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt,
        rejectionCodeOpt
      ) ++ paymentModelOptSeq ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("PaymentToPaymentFailure", "PR Retention", data: _*)
  }
}

object PaymentToExitAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], businessDetailsModelOpt: Option[BusinessDetailsModel] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModelOpt)

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("PaymentToExit", "PR Retention", data: _*)
  }
}

object PaymentToMicroServiceErrorAuditMessage {

  def from(transactionId: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val rejectionCodeOpt = Some(("rejectionCode", ErrorCodes.MicroServiceErrorCode + " - microservice_error"))

      Seq(
        transactionIdOpt,
        rejectionCodeOpt
      ).flatten // Remove empty values from list
    }
    Message("PaymentToMicroServiceError", "PR Retention", data: _*)
  }
}