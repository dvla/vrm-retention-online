package webserviceclients.vehicleandkeeperlookup

import play.api.libs.json.Json
import models.VehicleAndKeeperLookupFormModel

final case class VehicleAndKeeperDetailsRequest(referenceNumber: String,
                                                registrationNumber: String)

object VehicleAndKeeperDetailsRequest {

  implicit val JsonFormat = Json.format[VehicleAndKeeperDetailsRequest]

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel): VehicleAndKeeperDetailsRequest = {
    VehicleAndKeeperDetailsRequest(
      referenceNumber = vehicleAndKeeperLookupFormModel.referenceNumber,
      registrationNumber = vehicleAndKeeperLookupFormModel.registrationNumber
    )
  }
}