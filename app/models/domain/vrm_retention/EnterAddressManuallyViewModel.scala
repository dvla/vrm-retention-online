package models.domain.vrm_retention

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
      vehicleMake = vehicleAndKeeperDetailsModel.vehicleMake,
      vehicleModel = vehicleAndKeeperDetailsModel.vehicleModel,
      businessName = setupBusinessDetailsFormModel.businessName,
      businessContact = setupBusinessDetailsFormModel.businessContact,
      businessEmail = setupBusinessDetailsFormModel.businessEmail,
      businessPostCode = formatPostcode(setupBusinessDetailsFormModel.businessPostcode)
    )
}


