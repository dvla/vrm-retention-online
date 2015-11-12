package models

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

final case class ConfirmViewModel(registrationNumber: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel],
                                  userType: String,
                                  vehicleDetails: VehicleAndKeeperDetailsModel)

object ConfirmViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel, userType: String): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
      userType = userType,
      vehicleDetails = vehicleAndKeeperDetails
    )
}