package viewmodels

import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode

final case class EnterAddressManuallyViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String],
                                               businessName: String,
                                               businessContact: String,
                                               businessEmail: String,
                                               businessPostCode: String)

object EnterAddressManuallyViewModel {

  def apply(businessDetailsForm: SetupBusinessDetailsFormModel,
            vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): EnterAddressManuallyViewModel =
    EnterAddressManuallyViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      businessName = businessDetailsForm.name,
      businessContact = businessDetailsForm.contact,
      businessEmail = businessDetailsForm.email,
      businessPostCode = formatPostcode(businessDetailsForm.postcode)
    )
}


