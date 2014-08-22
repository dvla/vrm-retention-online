package viewmodels

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

final case class ConfirmViewModel(registrationNumber: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel],
                                  businessName: Option[String],
                                  businessContact: Option[String],
                                  businessEmail: Option[String],
                                  businessAddress: Option[AddressModel])

object ConfirmViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            businessDetailsModel: Option[BusinessDetailsModel]): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
      businessName = if (businessDetailsModel.isDefined) Some(businessDetailsModel.get.name) else None,
      businessContact = if (businessDetailsModel.isDefined) Some(businessDetailsModel.get.contact) else None,
      businessEmail = if (businessDetailsModel.isDefined) Some(businessDetailsModel.get.email) else None,
      businessAddress = if (businessDetailsModel.isDefined) Some(businessDetailsModel.get.address) else None
    )
}