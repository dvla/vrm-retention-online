package models.domain.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

final case class SuccessViewModel(registrationNumber: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel],
                                  businessName: Option[String],
                                  businessContact: Option[String],
                                  businessAddress: Option[AddressModel],
                                  replacementRegistrationNumber: String,
                                  retentionCertificationNumber: String,
                                  transactionId: String,
                                  transactionTimestamp: String)

object SuccessViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            eligibilityModel: EligibilityModel,
            businessDetailsModel: BusinessDetailsModel,
            retainModel: RetainModel): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
      businessName = Some(businessDetailsModel.businessName),
      businessContact = Some(businessDetailsModel.businessContact),
      businessAddress = Some(businessDetailsModel.businessAddress),
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
      businessName = None,
      businessContact = None,
      businessAddress = None,
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      retainModel.transactionId,
      retainModel.transactionTimestamp
    )
  }
}