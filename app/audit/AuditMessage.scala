package audit

import models.{PaymentModel, VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import java.util.UUID

//
// base classes
//
case class Message(name: String, serviceType: String, data: (String, Any)*) {
  /**
   * Unique message identifier.
   */
  var messageId = UUID.randomUUID

  def getDataAsJava = {
    import scala.collection.JavaConverters._
    data.toMap.asJava
  }
}

//
// concrete classes
//
object VehicleLookupToConfirmAuditMessage {

  def from(transactionId: String,
           vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      // TODO keeper address

      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
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
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      // TODO keeper address
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToConfirmBusiness", "PR Retention", data: _*)

  }

}

object VehicleLookupToSetUpBusinessDetailsAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      // TODO keeper address
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
        currentVrmOpt,
        replacementVRMOpt
      ).flatten // Remove empty values from list
    }
    Message("VehicleLookupToSetUpBusinessDetails", "PR Retention", data: _*)

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

object ConfirmBusinessToConfirmAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           currentVRM: String, replacementVRM: String) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
      val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      // TODO keeper address
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
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
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      // TODO keeper address
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
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
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      // TODO keeper address
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
      val paymentStatusOpt = Some(("paymentStatus", "Settled"))
      val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
      val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
      val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
      val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
      val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(totalAmountPaid => ("paymentTotalAmountPaid", totalAmountPaid / 100.0))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
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
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      // TODO keeper address
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
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
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
      val keeperTitleOpt = vehicleAndKeeperDetailsModel.title.map(keeperTitle => ("keeperTitle", keeperTitle))
      val keeperFirstNameOpt = vehicleAndKeeperDetailsModel.firstName.map(keeperFirstName => ("keeperFirstName", keeperFirstName))
      val keeperLastNameOpt = vehicleAndKeeperDetailsModel.lastName.map(keeperLastName => ("keeperLastName", keeperLastName))
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      // TODO keeper address
      val currentVrmOpt = Some(("currentVRM", vehicleAndKeeperLookupFormModel.registrationNumber))
      val replacementVRMOpt = Some(("replacementVRM", replacementVRM))
      val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
      val paymentStatusOpt = Some(("paymentStatus", "Cancelled"))
      val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
      val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
      val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
      val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
      val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
      val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(totalAmountPaid => ("paymentTotalAmountPaid", totalAmountPaid / 100.0))

      Seq(
        transactionIdOpt,
        makeOpt,
        modelOpt,
        keeperTitleOpt,
        keeperFirstNameOpt,
        keeperLastNameOpt,
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