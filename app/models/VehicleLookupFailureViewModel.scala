package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VehicleLookupFailureViewModel(registrationNumber: String,
                                               make: Option[String],
                                               model: Option[String],
                                               vehicleDetails: VehicleAndKeeperDetailsModel)

object VehicleLookupFailureViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      make = vehicleAndKeeperDetails.make,
      model = vehicleAndKeeperDetails.model,
      vehicleDetails = vehicleAndKeeperDetails
    )

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupForm.registrationNumber),
      make = None,
      model = None,
      VehicleAndKeeperDetailsModel(
        registrationNumber = formatVrm(vehicleAndKeeperLookupForm.registrationNumber),
        make = None,
        model = None,
        title = None,
        firstName = None,
        lastName = None,
        address = None,
        disposeFlag = None,
        keeperEndDate = None,
        keeperChangeDate = None,
        suppressedV5Flag = None
      )
    )
}