package models

import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode

final case class BusinessChooseYourAddressViewModel(registrationNumber: String,
                                                    vehicleMake: Option[String],
                                                    vehicleModel: Option[String],
                                                    name: String,
                                                    contact: String,
                                                    email: String,
                                                    postCode: String)

object BusinessChooseYourAddressViewModel {

  def apply(businessDetailsForm: SetupBusinessDetailsFormModel,
            vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): BusinessChooseYourAddressViewModel =
    BusinessChooseYourAddressViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      name = businessDetailsForm.name,
      contact = businessDetailsForm.contact,
      email = businessDetailsForm.email,
      postCode = formatPostcode(businessDetailsForm.postcode)
    )
}