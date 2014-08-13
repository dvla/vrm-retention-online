package models.domain.vrm_retention

import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode

final case class BusinessChooseYourAddressViewModel(registrationNumber: String,
                                                    vehicleMake: Option[String],
                                                    vehicleModel: Option[String],
                                                    businessName: String,
                                                    businessContact: String,
                                                    businessPostCode: String)

object BusinessChooseYourAddressViewModel {

  def apply(setupBusinessDetailsFormModel: SetupBusinessDetailsFormModel,
            vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): BusinessChooseYourAddressViewModel =
    BusinessChooseYourAddressViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      businessName = setupBusinessDetailsFormModel.businessName,
      businessContact = setupBusinessDetailsFormModel.businessContact,
      businessPostCode = formatPostcode(setupBusinessDetailsFormModel.businessPostcode)
    )
}