package models

import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VrmLockedViewModel(registrationNumber: String,
                                    vehicleMake: Option[String],
                                    vehicleModel: Option[String],
                                    timeString: String,
                                    javascriptTimestamp: Long)

object VrmLockedViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            timeString: String,
            javascriptTimestamp: Long): VrmLockedViewModel =
    VrmLockedViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      timeString,
      javascriptTimestamp
    )

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
            timeString: String,
            javascriptTimestamp: Long): VrmLockedViewModel =
    VrmLockedViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupForm.registrationNumber),
      vehicleMake = None,
      vehicleModel = None,
      timeString,
      javascriptTimestamp
    )
}