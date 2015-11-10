package models

import uk.gov.dvla.vehicles.presentation.common
import common.model.AddressModel
import common.model.VehicleAndKeeperDetailsModel

final case class SetupBusinessDetailsViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String],
                                               title: Option[String],
                                               firstName: Option[String],
                                               lastName: Option[String],
                                               address: Option[AddressModel],
                                               vehicleDetails: VehicleAndKeeperDetailsModel)

object SetupBusinessDetailsViewModel {
  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): SetupBusinessDetailsViewModel =
    SetupBusinessDetailsViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      title = vehicleAndKeeperDetails.title,
      firstName = vehicleAndKeeperDetails.firstName,
      lastName = vehicleAndKeeperDetails.lastName,
      address = vehicleAndKeeperDetails.address,
      vehicleDetails = vehicleAndKeeperDetails
    )
}