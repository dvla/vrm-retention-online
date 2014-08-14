package viewmodels

import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VehicleLookupFailureViewModel(registrationNumber: String,
                                               make: Option[String],
                                               model: Option[String])

object VehicleLookupFailureViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      make = vehicleAndKeeperDetails.make,
      model = vehicleAndKeeperDetails.model)

  def apply(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupFormModel.registrationNumber),
      make = None,
      model = None)
}