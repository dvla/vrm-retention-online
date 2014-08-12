package services.address_lookup

import models.domain.common.AddressViewModel
import play.api.i18n.Lang
import scala.concurrent.Future

trait AddressLookupService {

  def fetchAddressesForPostcode(postcode: String, trackingId: String)
                               (implicit lang: Lang): Future[Seq[(String, String)]]

  def fetchAddressForUprn(uprn: String, trackingId: String)
                         (implicit lang: Lang): Future[Option[AddressViewModel]]
}