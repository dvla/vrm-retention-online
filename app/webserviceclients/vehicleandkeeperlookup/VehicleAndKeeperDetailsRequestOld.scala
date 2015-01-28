package webserviceclients.vehicleandkeeperlookup

import models.VehicleAndKeeperLookupFormModel
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsString, JsValue, Writes, Json}

final case class VehicleAndKeeperDetailsRequestOld(referenceNumber: String,
                                                registrationNumber: String,
                                                transactionTimestamp: DateTime)

object VehicleAndKeeperDetailsRequestOld {

  // Handles this type of formatted string 2014-03-04T00:00:00.000Z
  implicit val jodaISODateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime): JsValue = {
      val formatter = ISODateTimeFormat.dateTime
      JsString(formatter.print(dateTime))
    }
  }

  implicit val JsonFormat = Json.format[VehicleAndKeeperDetailsRequestOld]

  def from(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel, transactionTimestamp: DateTime): VehicleAndKeeperDetailsRequestOld = {
    VehicleAndKeeperDetailsRequestOld(
      referenceNumber = vehicleAndKeeperLookupFormModel.referenceNumber,
      registrationNumber = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = transactionTimestamp
    )
  }
}