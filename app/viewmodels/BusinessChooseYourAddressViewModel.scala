package viewmodels

import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode

final case class BusinessChooseYourAddressViewModel(registrationNumber: String,
                                                    vehicleMake: Option[String],
                                                    vehicleModel: Option[String],
                                                    name: String,
                                                    contact: String,
                                                    email: String,
                                                    postCode: String)

object BusinessChooseYourAddressViewModel {

  def apply(setupBusinessDetailsFormModel: SetupBusinessDetailsFormModel,
            vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): BusinessChooseYourAddressViewModel =
    BusinessChooseYourAddressViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      name = setupBusinessDetailsFormModel.name,
      contact = setupBusinessDetailsFormModel.contact,
      email = setupBusinessDetailsFormModel.email,
      postCode = formatPostcode(setupBusinessDetailsFormModel.postcode)
    )
}