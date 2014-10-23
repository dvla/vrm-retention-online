package audit

import java.util.UUID
import models.{BusinessDetailsModel, PaymentModel, VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import org.joda.time.format.ISODateTimeFormat
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl


//
// base classes
//
case class Message(name: String, serviceType: String, data: (String, Any)*) {

  var messageId = UUID.randomUUID

  def getDataAsJava = {
    import scala.collection.JavaConverters._
    data.toMap.asJava
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

object NameOptsToOptionString {

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

object KeeperAddressToOptionString {

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

object BusinessAddressToOptionString {

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

//
// concrete classes
//


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
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = businessDetailsModelOpt match {
        case Some(businessDetailsModel) => {
          val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
          val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
            businessAddress => ("businessAddress", businessAddress))
          val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
          Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
        }
        case _ => Seq.empty
      }

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
      val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
      val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
        businessAddress => ("businessAddress", businessAddress))
      val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        businessNameOpt,
        businessAddressOpt,
        businessEmailOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list
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
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
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

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))

      Seq(
        transactionIdOpt,
        timestampOpt,
        currentVrmOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToVehicleLookupFailure", "PR Retention", data: _*)

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
      val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
      val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
        businessAddress => ("businessAddress", businessAddress))
      val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        businessNameOpt,
        businessAddressOpt,
        businessEmailOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list
    }
    Message("CaptureActorToConfirmBusiness", "PR Retention", data: _*)

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
      val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
      val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
        businessAddress => ("businessAddress", businessAddress))
      val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        businessNameOpt,
        businessAddressOpt,
        businessEmailOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list
    }
    Message("ConfirmBusinessToConfirm", "PR Retention", data: _*)

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
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = businessDetailsModelOpt match {
        case Some(businessDetailsModel) => {
          val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
          val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
            businessAddress => ("businessAddress", businessAddress))
          val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
          Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
        }
        case _ => Seq.empty
      }

      (Seq(
        transactionIdOpt,
        timestampOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("ConfirmToPayment", "PR Retention", data: _*)

  }

}

object PaymentToSuccessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String],
           businessDetailsModelOpt: Option[BusinessDetailsModel] = None, paymentModel: PaymentModel,
           retentionCertId: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
      val paymentStatusOpt = Some(("paymentStatus", "Settled"))
      val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
      val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
      val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
      val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
      val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(
        totalAmountPaid => ("paymentTotalAmountPaid", totalAmountPaid / 100.0))
      val retentionCertIdOpt = Some(("retentionCertId", retentionCertId))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = businessDetailsModelOpt match {
        case Some(businessDetailsModel) => {
          val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
          val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
            businessAddress => ("businessAddress", businessAddress))
          val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
          Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
        }
        case _ => Seq.empty
      }

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
        paymentTrxRefOpt,
        paymentStatusOpt,
        paymentMaskedPanOpt,
        paymentAuthCodeOpt,
        paymentMerchantIdOpt,
        paymentTypeOpt,
        paymentCardTypeOpt,
        paymentTotalAmountPaidOpt,
        retentionCertIdOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("PaymentToSuccess", "PR Retention", data: _*)

  }

}

object PaymentToPaymentNotAuthorisedAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], businessDetailsModelOpt: Option[BusinessDetailsModel] = None,
           paymentModel: PaymentModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
      val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
      val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
      val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
      val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
      val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(
        totalAmountPaid => ("paymentTotalAmountPaid", (totalAmountPaid / 100.0)))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = businessDetailsModelOpt match {
        case Some(businessDetailsModel) => {
          val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
          val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
            businessAddress => ("businessAddress", businessAddress))
          val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
          Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
        }
        case _ => Seq.empty
      }

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
        paymentTrxRefOpt,
        paymentMaskedPanOpt,
        paymentAuthCodeOpt,
        paymentMerchantIdOpt,
        paymentTypeOpt,
        paymentCardTypeOpt,
        paymentTotalAmountPaidOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("PaymentToPaymentNotAuthorised", "PR Retention", data: _*)

  }
}

object PaymentToPaymentFailureAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], businessDetailsModelOpt: Option[BusinessDetailsModel] = None,
           paymentModel: PaymentModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", Timestamp.getTimestamp))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(
        keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = KeeperAddressToOptionString.from(vehicleAndKeeperDetailsModel.address).map(
        keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
      val paymentStatusOpt = Some(("paymentStatus", "Cancelled"))
      val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
      val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
      val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
      val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
      val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(
        totalAmountPaid => ("paymentTotalAmountPaid", totalAmountPaid / 100.0))

      // may have got to the confirm screen down the keeper route hence no business details
      val businessDetailsModelOptSeq = businessDetailsModelOpt match {
        case Some(businessDetailsModel) => {
          val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
          val businessAddressOpt = BusinessAddressToOptionString.from(businessDetailsModel).map(
            businessAddress => ("businessAddress", businessAddress))
          val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
          Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
        }
        case _ => Seq.empty
      }

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
        paymentTrxRefOpt,
        paymentStatusOpt,
        paymentMaskedPanOpt,
        paymentAuthCodeOpt,
        paymentMerchantIdOpt,
        paymentTypeOpt,
        paymentCardTypeOpt,
        paymentTotalAmountPaidOpt
      ) ++ businessDetailsModelOptSeq).flatten // Remove empty values from list
    }
    Message("PaymentToPaymentFailure", "PR Retention", data: _*)

  }

}