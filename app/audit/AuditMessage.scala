package audit

import models.{PaymentModel, VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel}
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel


//
// base classes
//

trait AuditMessage {
}

case class VehicleAuditDetails(make: Option[String], model: Option[String])

case class VrmAuditDetails(retained: String, replacement: Option[String])

case class KeeperAuditDetails(title: Option[String], firstName: Option[String], lastName: Option[String],
                              address: Option[AddressModel], email: Option[String])

object KeeperAuditDetails {

  def from(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, keeperEmail: Option[String]) = {
    KeeperAuditDetails(
      vehicleAndKeeperDetailsModel.title,
      vehicleAndKeeperDetailsModel.firstName,
      vehicleAndKeeperDetailsModel.lastName,
      vehicleAndKeeperDetailsModel.address,
      keeperEmail
    )
  }
}

case class BusinessAuditDetails(contact: String, email: String)

case class PaymentAuditDetails(trxRef: Option[String], paymentStatus: Option[String] = None,
                               maskedPAN: Option[String] = None, authCode: Option[String] = None,
                               merchantId: Option[String] = None, paymentType: Option[String] = None,
                               cardType: Option[String] = None, totalAmountPaid: Option[Double] = None,
                               rejectionCode: Option[String] = None)


//
// concrete classes
//

final case class VehicleLookupToConfirmAuditMessage(transactionId: String,
                                                    vehicleAuditDetails: VehicleAuditDetails,
                                                    vrmAuditDetails: VrmAuditDetails) extends AuditMessage {
}

object VehicleLookupToConfirmAuditMessage {

  def from(transactionId: String,
           vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    VehicleLookupToConfirmAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)))
  }

}

final case class VehicleLookupToConfirmBusinessAuditMessage(transactionId: String, vehicleAuditDetails: VehicleAuditDetails,
                                                    vrmAuditDetails: VrmAuditDetails) extends AuditMessage {
}

object VehicleLookupToConfirmBusinessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    VehicleLookupToConfirmBusinessAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)))
  }

}


final case class VehicleLookupToSetUpBusinessDetailsAuditMessage(transactionId: String, vehicleAuditDetails: VehicleAuditDetails,
                                                    vrmAuditDetails: VrmAuditDetails) extends AuditMessage {
}

object VehicleLookupToSetUpBusinessDetailsAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String) = {

    VehicleLookupToSetUpBusinessDetailsAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)))
  }

}

final case class VehicleLookupToVehicleLookupFailureAuditMessage(transactionId: String,
                                                                 vehicleAuditDetails: Option[VehicleAuditDetails],
                                                                 vrmAuditDetails: VrmAuditDetails) extends AuditMessage {
}

object VehicleLookupToVehicleLookupFailureAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel] = None) = {

    VehicleLookupToVehicleLookupFailureAuditMessage(transactionId,
      if (vehicleAndKeeperDetailsModel.isDefined) {
        Some(VehicleAuditDetails(vehicleAndKeeperDetailsModel.get.make, vehicleAndKeeperDetailsModel.get.model))
      } else None,
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, None))
  }

}

final case class ConfirmBusinessToConfirmAuditMessage(transactionId: String, vehicleAuditDetails: VehicleAuditDetails,
                                                                 vrmAuditDetails: VrmAuditDetails) extends AuditMessage {
}

object ConfirmBusinessToConfirmAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           currentVRM: String, replacementVRM: String) = {

    ConfirmBusinessToConfirmAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)))
  }

}

final case class ConfirmToPaymentAuditMessage(transactionId: String, vehicleAuditDetails: VehicleAuditDetails,
                                                      vrmAuditDetails: VrmAuditDetails,
                                                      keeperAuditDetails: KeeperAuditDetails) extends AuditMessage {
}

object ConfirmToPaymentAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           currentVRM: String, replacementVRM: String, keeperEmail: Option[String]) = {

    ConfirmToPaymentAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      KeeperAuditDetails.from(vehicleAndKeeperDetailsModel, keeperEmail))
  }

}

final case class PaymentToSuccessAuditMessage(transactionId: String,
                                              vehicleAuditDetails: VehicleAuditDetails,
                                              vrmAuditDetails: VrmAuditDetails,
                                              keeperAuditDetails: KeeperAuditDetails,
                                              paymentAuditDetails: PaymentAuditDetails) extends AuditMessage {
}

object PaymentToSuccessAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], paymentModel: PaymentModel) = {

    PaymentToSuccessAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      KeeperAuditDetails.from(vehicleAndKeeperDetailsModel, keeperEmail),
      PaymentAuditDetails(trxRef = paymentModel.trxRef,
        paymentStatus = Some("Settled"),
        maskedPAN = paymentModel.maskedPAN,
        authCode = paymentModel.authCode,
        merchantId = paymentModel.merchantId,
        paymentType = paymentModel.paymentType,
        cardType = paymentModel.cardType,
        totalAmountPaid = Some((paymentModel.totalAmountPaid.get / 100.0))))
  }

}

final case class PaymentToPaymentNotAuthorisedAuditMessage(transactionId: String,
                                                           vehicleAuditDetails: VehicleAuditDetails,
                                                           vrmAuditDetails: VrmAuditDetails,
                                                           keeperAuditDetails: KeeperAuditDetails,
                                                           paymentAuditDetails: PaymentAuditDetails) extends AuditMessage {
}

object PaymentToPaymentNotAuthorisedAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String],  paymentModel: PaymentModel) = {

    PaymentToPaymentNotAuthorisedAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      KeeperAuditDetails.from(vehicleAndKeeperDetailsModel, keeperEmail),
      PaymentAuditDetails(trxRef = paymentModel.trxRef))
  }

}

final case class PaymentToPaymentFailureAuditMessage(transactionId: String,
                                                     vehicleAuditDetails: VehicleAuditDetails,
                                                           vrmAuditDetails: VrmAuditDetails,
                                                           keeperAuditDetails: KeeperAuditDetails,
                                                           paymentAuditDetails: PaymentAuditDetails) extends AuditMessage {
}

object PaymentToPaymentFailureAuditMessage {

  def from(transactionId: String, vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           replacementVRM: String, keeperEmail: Option[String], paymentModel: PaymentModel) = {

    PaymentToPaymentFailureAuditMessage(transactionId,
      VehicleAuditDetails(vehicleAndKeeperDetailsModel.make, vehicleAndKeeperDetailsModel.model),
      VrmAuditDetails(vehicleAndKeeperLookupFormModel.registrationNumber, Some(replacementVRM)),
      KeeperAuditDetails.from(vehicleAndKeeperDetailsModel, keeperEmail),
      PaymentAuditDetails(trxRef = paymentModel.trxRef))
  }

}