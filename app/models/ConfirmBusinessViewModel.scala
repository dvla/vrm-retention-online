package models

import uk.gov.dvla.vehicles.presentation.common
import common.model.AddressModel
import common.model.VehicleAndKeeperDetailsModel

final case class ConfirmBusinessViewModel(vehicleDetails: VehicleAndKeeperDetailsModel,
                                          businessName: Option[String],
                                          businessContact: Option[String],
                                          businessEmail: Option[String],
                                          businessAddress: Option[AddressModel]
                                           )

object ConfirmBusinessViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            businessDetailsOpt: Option[BusinessDetailsModel]): ConfirmBusinessViewModel =
    ConfirmBusinessViewModel(
      vehicleDetails = vehicleAndKeeperDetails,
      businessName = businessDetailsOpt.map(_.name),
      businessContact = businessDetailsOpt.map(_.contact),
      businessEmail = businessDetailsOpt.map(_.email),
      businessAddress = businessDetailsOpt.map(_.address)
    )
}