package audit

import java.util.UUID
import models.{PaymentModel, VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

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
// concrete classes
//

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

object AddressSeqToOptionString {

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

object VehicleLookupToConfirmAuditMessage {

  def from(transactionId: String,
           vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list

    }
    Message("VehicleLookupToConfirm", "PR Retention", data: _*)
  }

}

object VehicleLookupToConfirmBusinessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      // TODO business details
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
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
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
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
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))

      Seq(
        transactionIdOpt,
        currentVrmOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToVehicleLookupFailure", "PR Retention", data: _*)

  }

}

object CaptureActorToConfirmBusinessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
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
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           currentVRM: String, replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
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
           currentVRM: String, replacementVRM: String, keeperEmail: Option[String]) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperNameOpt,
        keeperAddressOpt,
        currentVrmOpt,
        currentVrmOpt,
        replacementVRMOpt,
        keeperEmailOpt
      ).flatten // Remove empty values from list
    }
    Message("ConfirmToPayment", "PR Retention", data: _*)

  }

}

object PaymentToSuccessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], paymentModel: PaymentModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
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
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(totalAmountPaid => ("paymentTotalAmountPaid", (totalAmountPaid / 100.0)))

      Seq(
        transactionIdOpt,
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
      ).flatten // Remove empty values from list
    }
    Message("PaymentToSuccess", "PR Retention", data: _*)

  }

}

object PaymentToPaymentNotAuthorisedAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], paymentModel: PaymentModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
      val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
      val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
      val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
      val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
      val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(totalAmountPaid => ("paymentTotalAmountPaid", (totalAmountPaid / 100.0)))

      Seq(
        transactionIdOpt,
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
      ).flatten // Remove empty values from list
    }
    Message("PaymentToPaymentNotAuthorised", "PR Retention", data: _*)

  }
}

object PaymentToPaymentFailureAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], paymentModel: PaymentModel) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperNameOpt = NameOptsToOptionString.from(vehicleAndKeeperDetailsModel).map(keeperName => ("keeperName", keeperName))
      val keeperAddressOpt = AddressSeqToOptionString.from(vehicleAndKeeperDetailsModel.address).map(keeperAddress => ("keeperAddress", keeperAddress))
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
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(totalAmountPaid => ("paymentTotalAmountPaid", (totalAmountPaid / 100.0)))

      Seq(
        transactionIdOpt,
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
      ).flatten // Remove empty values from list
    }
    Message("PaymentToPaymentFailure", "PR Retention", data: _*)

  }

}