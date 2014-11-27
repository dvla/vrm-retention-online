package webserviceclients.vehicleandkeeperlookup

import models.VehicleAndKeeperLookupFormModel
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsString, JsValue, Writes, Json}

final case class VehicleAndKeeperDetailsRequest(referenceNumber: String,
                                                registrationNumber: String,
                                                transactionTimestamp: DateTime)

object VehicleAndKeeperDetailsRequest {

  // Handles this type of formatted string 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }

  implicit val JsonFormat = Json.format[VehicleAndKeeperDetailsRequest]

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel, transactionTimestamp: DateTime): VehicleAndKeeperDetailsRequest = {
    VehicleAndKeeperDetailsRequest(
      referenceNumber = vehicleAndKeeperLookupFormModel.referenceNumber,
      registrationNumber = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = transactionTimestamp
    )
  }
}