package webserviceclients.vehicleandkeeperlookup

import play.api.libs.json.Json

final case class VehicleAndKeeperDetailsDto(registrationNumber: String,
                                            vehicleMake: Option[String],
                                            vehicleModel: Option[String],
                                            keeperTitle: Option[String],
                                            keeperFirstName: Option[String],
                                            keeperLastName: Option[String],
                                            keeperAddressLine1: Option[String],
                                            keeperAddressLine2: Option[String],
                                            keeperAddressLine3: Option[String],
                                            keeperAddressLine4: Option[String],
                                            keeperPostTown: Option[String],
                                            keeperPostcode: Option[String])

object VehicleAndKeeperDetailsDto {

  implicit val JsonFormat = Json.format[VehicleAndKeeperDetailsDto]
}