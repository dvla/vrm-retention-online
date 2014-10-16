package audit

import models.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}

trait AuditMessage {
}

case class VehicleAuditDetails(make: Option[String], model: Option[String])

case class VrmAuditDetails(retained: Option[String], replacement: Option[String])

case class KeeperAuditDetails(email: Option[String])

case class BusinessAuditDetails(contact: String, email: String)

case class PaymentAuditDetails(trxRef: String, maskedPAN: Option[String], authCode: Option[String],
                               merchantId: Option[String], paymentType: Option[String], cardType: Option[String],
                               totalAmountPaid: Option[Double])



final case class VehicleLookupToConfirmAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                                    vrmAuditDetails: VrmAuditDetails,
                                                    transactionId: String) extends AuditMessage {
}

object VehicleLookupToConfirmAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           currentVRM: String, replacementVRM: String) = {

    VehicleLookupToConfirmAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(Some(currentVRM), Some(replacementVRM)),
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
           currentVRM: String, replacementVRM: String) = {

    VehicleLookupToSetUpBusinessDetailsAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(Some(currentVRM), Some(replacementVRM)),
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

    VehicleLookupToSetUpBusinessDetailsAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(Some(currentVRM), Some(replacementVRM)),
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
      VrmAuditDetails(Some(currentVRM), Some(replacementVRM)),
      transactionId,
      KeeperAuditDetails(keeperEmail))
  }

}

final case class PaymentToSuccessAuditMessage(vehicleAuditDetails: VehicleAuditDetails,
                                              vrmAuditDetails: VrmAuditDetails,
                                              transactionId: String,
                                              keeperAuditDetails: KeeperAuditDetails) extends AuditMessage {
}

object PaymentToSuccessAuditMessage {

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, transactionId: String,
           currentVRM: String, replacementVRM: String, keeperEmail: Option[String]) = {

    PaymentToSuccessAuditMessage(
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(Some(currentVRM), Some(replacementVRM)),
      transactionId,
      KeeperAuditDetails(keeperEmail))
  }

}