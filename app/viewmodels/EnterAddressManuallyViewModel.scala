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

  def apply(setupBusinessDetailsFormModel: SetupBusinessDetailsFormModel,
            vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): EnterAddressManuallyViewModel =
    EnterAddressManuallyViewModel(
      registrationNumber = vehicleAndKeeperDetailsModel.registrationNumber,
      vehicleMake = vehicleAndKeeperDetailsModel.make,
      vehicleModel = vehicleAndKeeperDetailsModel.model,
      businessName = setupBusinessDetailsFormModel.name,
      businessContact = setupBusinessDetailsFormModel.contact,
      businessEmail = setupBusinessDetailsFormModel.email,
      businessPostCode = formatPostcode(setupBusinessDetailsFormModel.postcode)
    )
}


