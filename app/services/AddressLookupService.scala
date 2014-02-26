package services

import models.domain.disposal_of_vehicle.AddressViewModel
import scala.concurrent.Future
import play.api.libs.ws.Response
import services.ordnance_survey.domain.OSAddressbaseResult

trait AddressLookupService {
  protected def callWebService(postcode: String): Future[Response]
  protected def extractFromJson(resp: Response): Option[Seq[OSAddressbaseResult]]
  def fetchAddressesForPostcode(postcode: String): Future[Seq[(String, String)]]
  def fetchAddressForUprn(uprn: String): Future[Option[AddressViewModel]]
}