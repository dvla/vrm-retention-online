package services.address_lookup

import scala.concurrent.Future
import play.api.i18n.Lang
import models.domain.common.AddressViewModel

trait AddressLookupService {

  def fetchAddressesForPostcode(postcode: String, trackingId: String)
                               (implicit lang: Lang): Future[Seq[(String, String)]]

  def fetchAddressForUprn(uprn: String, trackingId: String)
                         (implicit lang: Lang): Future[Option[AddressViewModel]]
}