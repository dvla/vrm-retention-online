package models.domain.vrm_retention

import constraints.common.RegistrationNumber.formatVrm

final case class VehicleLookupFailureViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String])

object VehicleLookupFailureViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel)

  def apply(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupFormModel.registrationNumber),
      vehicleMake = None,
      vehicleModel = None)
}