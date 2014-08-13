package models.domain.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

final case class SuccessViewModel(registrationNumber: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel],
                                  keeperEmail: Option[String],
                                  businessName: Option[String],
                                  businessContact: Option[String],
                                  businessEmail: Option[String],
                                  businessAddress: Option[AddressModel],
                                  replacementRegistrationNumber: String,
                                  retentionCertificationNumber: String,
                                  transactionId: String,
                                  transactionTimestamp: String)

object SuccessViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            eligibilityModel: EligibilityModel,
            businessDetailsModel: BusinessDetailsModel,
            confirmFormModel: ConfirmFormModel,
            retainModel: RetainModel): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
      keeperEmail = confirmFormModel.keeperEmail,
      businessName = Some(businessDetailsModel.businessName),
      businessContact = Some(businessDetailsModel.businessContact),
      businessEmail = Some(businessDetailsModel.businessEmail),
      businessAddress = Some(businessDetailsModel.businessAddress),
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      retainModel.transactionId,
      retainModel.transactionTimestamp
    )
  }

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            eligibilityModel: EligibilityModel,
            confirmFormModel: ConfirmFormModel,
            retainModel: RetainModel): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
      keeperEmail = confirmFormModel.keeperEmail,
      businessName = None,
      businessContact = None,
      businessEmail = None,
      businessAddress = None,
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      retainModel.transactionId,
      retainModel.transactionTimestamp
    )
  }

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            eligibilityModel: EligibilityModel,
            retainModel: RetainModel): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
      keeperEmail = None,
      businessName = None,
      businessContact = None,
      businessEmail = None,
      businessAddress = None,
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      retainModel.transactionId,
      retainModel.transactionTimestamp
    )
  }
}