package viewmodels

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
            businessDetailsModelOpt: Option[BusinessDetailsModel],
            keeperEmail: Option[String],
            retainModel: RetainModel,
            transactionId: String): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
      keeperEmail = keeperEmail,
      businessName = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.name
      },
      businessContact = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.contact
      },
      businessEmail = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.email
      },
      businessAddress = for (businessDetails <- businessDetailsModelOpt) yield {
        businessDetails.address
      },
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      transactionId,
      retainModel.transactionTimestamp
    )
  }

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            eligibilityModel: EligibilityModel,
            retainModel: RetainModel,
            transactionId: String): SuccessViewModel = {
    SuccessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
      keeperEmail = None,
      businessName = None,
      businessContact = None,
      businessEmail = None,
      businessAddress = None,
      replacementRegistrationNumber = eligibilityModel.replacementVRM,
      retainModel.certificateNumber,
      transactionId,
      retainModel.transactionTimestamp
    )
  }
}