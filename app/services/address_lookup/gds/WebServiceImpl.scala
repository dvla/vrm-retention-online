package services.address_lookup.gds

import com.google.inject.Inject
import play.api.Logger
import play.api.i18n.Lang
import play.api.libs.ws.{Response, WS}
import services.HttpHeaders
import services.address_lookup.AddressLookupWebService
import utils.helpers.Config
import scala.concurrent.Future

final class WebServiceImpl @Inject()(config: Config) extends AddressLookupWebService {
  private val baseUrl: String = config.gdsAddressLookupBaseUrl
  private val authorisation: String = config.gdsAddressLookupAuthorisation
  private val requestTimeout: Int = config.gdsAddressLookupRequestTimeout

  def postcodeWithNoSpaces(postcode: String): String = postcode.filter(_ != ' ')

  // request should look like    (GET, "/addresses?postcode=kt70ej").withHeaders(validAuthHeader)
  override def callPostcodeWebService(postcode: String, trackingId: String)
                                     (implicit lang: Lang): Future[Response] = {
    val endPoint = s"$baseUrl/addresses?postcode=${ postcodeWithNoSpaces(postcode) }"
    Logger.debug(s"Calling GDS postcode lookup service on $endPoint...")
    WS.url(endPoint).
      withHeaders("AUTHORIZATION" -> authorisation).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(requestTimeout). // Timeout is in milliseconds
      get()
  }

  override def callUprnWebService(uprn: String, trackingId: String)
                                 (implicit lang: Lang): Future[Response] = {
    val endPoint = s"$baseUrl/uprn?uprn=$uprn"
    Logger.debug(s"Calling GDS uprn lookup service on $endPoint...")
    WS.url(endPoint).
      withHeaders("AUTHORIZATION" -> authorisation).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(requestTimeout). // Timeout is in milliseconds
      get()
  }
}