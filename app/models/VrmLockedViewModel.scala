package models

import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class VrmLockedViewModel(registrationNumber: String,
                                    vehicleMake: Option[String],
                                    vehicleModel: Option[String],
                                    timeString: String,
                                    javascriptTimestamp: Long,
                                    vehicleDetails: VehicleAndKeeperDetailsModel,
                                    v5ref: String,
                                    postcode: String)

object VrmLockedViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            timeString: String,
            javascriptTimestamp: Long): VrmLockedViewModel =
    VrmLockedViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      timeString,
      javascriptTimestamp,
      vehicleDetails = vehicleAndKeeperDetails,
      v5ref = "",
      postcode = ""
    )

  def apply(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
            timeString: String,
            javascriptTimestamp: Long): VrmLockedViewModel =
    VrmLockedViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupForm.registrationNumber),
      vehicleMake = None,
      vehicleModel = None,
      timeString,
      javascriptTimestamp,
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
      ),
      v5ref = vehicleAndKeeperLookupForm.referenceNumber,
      postcode = vehicleAndKeeperLookupForm.postcode
    )
}