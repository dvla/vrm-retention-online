package services.address_lookup

import play.api.i18n.Lang
import play.api.libs.ws.Response
import scala.concurrent.Future

// Wrapper around our webservice call so that we can IoC fake versions for testing or use the real version.
trait AddressLookupWebService {

  def callPostcodeWebService(postcode: String, trackingId: String)
                            (implicit lang: Lang): Future[Response]

  def callUprnWebService(uprn: String, trackingId: String)
                        (implicit lang: Lang): Future[Response]
}