package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VehicleLookupFailureViewModel(registrationNumber: String,
                                               v5ref: String,
                                               postcode: String,
                                               vehicleDetails: VehicleAndKeeperDetailsModel)

object VehicleLookupFailureViewModel {

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel, vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel]): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupForm.registrationNumber),
      v5ref = vehicleAndKeeperLookupForm.referenceNumber,
      postcode = vehicleAndKeeperLookupForm.postcode,
      vehicleAndKeeperDetails match {
        case Some(details) => details
        case None =>
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
      }
    )
}