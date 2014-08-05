package models.domain.vrm_retention

final case class SetupBusinessDetailsViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String])

object SetupBusinessDetailsViewModel {

  def apply(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): SetupBusinessDetailsViewModel =
    SetupBusinessDetailsViewModel(
      registrationNumber = vehicleAndKeeperDetailsModel.registrationNumber,
      vehicleMake = vehicleAndKeeperDetailsModel.vehicleMake,
      vehicleModel = vehicleAndKeeperDetailsModel.vehicleModel
    )
}