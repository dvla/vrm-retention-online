package viewmodels

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

final case class ConfirmViewModel(registrationNumber: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel],
                                  businessName: Option[String],
                                  businessContact: Option[String],
                                  businessEmail: Option[String],
                                  businessAddress: Option[AddressModel])

object ConfirmViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            businessDetailsModel: BusinessDetailsModel): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
      businessName = Some(businessDetailsModel.businessName),
      businessContact = Some(businessDetailsModel.businessContact),
      businessEmail = Some(businessDetailsModel.businessEmail),
      businessAddress = Some(businessDetailsModel.businessAddress)
    )

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel,
      keeperTitle = vehicleAndKeeperDetails.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetails.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetails.keeperLastName,
      keeperAddress = vehicleAndKeeperDetails.keeperAddress,
      None, None, None, None
    )
}