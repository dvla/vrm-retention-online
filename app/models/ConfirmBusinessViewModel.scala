package models

import uk.gov.dvla.vehicles.presentation.common.model.{AddressModel, VehicleAndKeeperDetailsModel}

final case class ConfirmBusinessViewModel(registrationNumber: String,
                                          vehicleMake: Option[String],
                                          vehicleModel: Option[String],
                                          businessName: Option[String],
                                          businessContact: Option[String],
                                          businessEmail: Option[String],
                                          businessAddress: Option[AddressModel])

object ConfirmBusinessViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            businessDetailsOpt: Option[BusinessDetailsModel]): ConfirmBusinessViewModel =
    ConfirmBusinessViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      businessName = businessDetailsOpt.map(_.name),
      businessContact = businessDetailsOpt.map(_.contact),
      businessEmail = businessDetailsOpt.map(_.email),
      businessAddress = businessDetailsOpt.map(_.address)
    )
}