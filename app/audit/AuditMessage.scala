package audit

import models.{PaymentModel, VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}

trait AuditMessage {
}

case class VehicleAuditDetails(make: Option[String], model: Option[String])

case class VrmAuditDetails(retained: String, replacement: Option[String])

case class KeeperAuditDetails(email: Option[String])

case class BusinessAuditDetails(contact: String, email: String)

case class PaymentAuditDetails(trxRef: Option[String], paymentStatus: Option[String] = None,
                               maskedPAN: Option[String] = None, authCode: Option[String] = None,
                               merchantId: Option[String] = None, paymentType: Option[String] = None,
                               cardType: Option[String] = None, totalAmountPaid: Option[Double] = None,
                               rejectionCode: Option[String] = None)


final case class VehicleLookupToConfirmAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                    vrmAuditDetails: VrmAuditDetails,
                                                    transactionId: String) extends AuditMessage {
}

object VehicleLookupToConfirmAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           replacementVRM: String) = {

    VehicleLookupToConfirmAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId)
  }

}

final case class VehicleLookupToConfirmBusinessAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                    vrmAuditDetails: VrmAuditDetails,
                                                    transactionId: String) extends AuditMessage {
}

object VehicleLookupToConfirmBusinessAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           replacementVRM: String) = {

    VehicleLookupToConfirmBusinessAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId)
  }

}


final case class VehicleLookupToSetUpBusinessDetailsAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                    vrmAuditDetails: VrmAuditDetails,
                                                    transactionId: String) extends AuditMessage {
}

object VehicleLookupToSetUpBusinessDetailsAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           replacementVRM: String) = {

    VehicleLookupToSetUpBusinessDetailsAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId)
  }

}

final case class VehicleLookupToVehicleLookupFailureAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                                 vrmAuditDetails: VrmAuditDetails,
                                                                 transactionId: String) extends AuditMessage {
}

object VehicleLookupToVehicleLookupFailureAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String) = {

    VehicleLookupToVehicleLookupFailureAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, None),
      transactionId)
  }

}

final case class ConfirmBusinessToConfirmAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                                 vrmAuditDetails: VrmAuditDetails,
                                                                 transactionId: String) extends AuditMessage {
}

object ConfirmBusinessToConfirmAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           currentVRM: String, replacementVRM: String) = {

    ConfirmBusinessToConfirmAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId)
  }

}

final case class ConfirmToPaymentAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                      vrmAuditDetails: VrmAuditDetails,
                                                      transactionId: String,
                                                      keeperAuditDetails: KeeperAuditDetails) extends AuditMessage {
}

object ConfirmToPaymentAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           currentVRM: String, replacementVRM: String, keeperEmail: Option[String]) = {

    ConfirmToPaymentAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId,
      KeeperAuditDetails(keeperEmail))
  }

}

final case class PaymentToSuccessAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                              vrmAuditDetails: VrmAuditDetails,
                                              transactionId: String,
                                              keeperAuditDetails: KeeperAuditDetails,
                                              paymentAuditDetails: PaymentAuditDetails) extends AuditMessage {
}

object PaymentToSuccessAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           replacementVRM: String, keeperEmail: Option[String], paymentModel: PaymentModel) = {

    PaymentToSuccessAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId,
      KeeperAuditDetails(keeperEmail),
      PaymentAuditDetails(trxRef = paymentModel.trxRef,
        maskedPAN = paymentModel.maskedPAN,
        authCode = paymentModel.authCode,
        paymentType = paymentModel.paymentType,
        cardType = paymentModel.cardType,
        totalAmountPaid = Some((paymentModel.totalAmountPaid.get / 100.0))))
  }

}

final case class PaymentToPaymentNotAuthorisedAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                           vrmAuditDetails: VrmAuditDetails,
                                                           transactionId: String,
                                                           keeperAuditDetails: KeeperAuditDetails,
                                                           paymentAuditDetails: PaymentAuditDetails) extends AuditMessage {
}

object PaymentToPaymentNotAuthorisedAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           replacementVRM: String, keeperEmail: Option[String],  paymentModel: PaymentModel) = {

    PaymentToPaymentNotAuthorisedAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId,  KeeperAuditDetails(keeperEmail),
      PaymentAuditDetails(trxRef = paymentModel.trxRef))
  }

}

final case class PaymentToPaymentFailureAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                           vrmAuditDetails: VrmAuditDetails,
                                                           transactionId: String,
                                                           keeperAuditDetails: KeeperAuditDetails,
                                                           paymentAuditDetails: PaymentAuditDetails) extends AuditMessage {
}

object PaymentToPaymentFailureAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           replacementVRM: String, keeperEmail: Option[String], paymentModel: PaymentModel) = {

    PaymentToPaymentFailureAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      transactionId,  KeeperAuditDetails(keeperEmail),
      PaymentAuditDetails(trxRef = paymentModel.trxRef))
  }

}