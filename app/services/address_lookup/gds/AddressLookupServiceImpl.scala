package services.address_lookup.gds

import javax.inject.Inject
import models.domain.common.AddressViewModel
import play.api.Logger
import play.api.i18n.Lang
import play.api.libs.ws.Response
import services.address_lookup.gds.domain.Address
import services.address_lookup.gds.domain.JsonFormats.addressFormat
import services.address_lookup.{AddressLookupService, AddressLookupWebService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class AddressLookupServiceImpl @Inject()(ws: AddressLookupWebService)
  extends AddressLookupService {

  private def extractFromJson(resp: Response): Seq[Address] =
    try resp.json.as[Seq[Address]]
    catch {
      case e: Throwable => Seq.empty //  return empty seq given invalid json
    }

  override def fetchAddressesForPostcode(postcode: String, trackingId: String)
                                        (implicit lang: Lang): Future[Seq[(String, String)]] = {
    def sort(addresses: Seq[Address]): Seq[Address] = {
      addresses.sortBy(addressDpa => {
        val buildingNumber = addressDpa.houseNumber.getOrElse("0")
        val buildingNumberSanitised = buildingNumber.replaceAll("[^0-9]", "")
        // Sanitise building number as it could contain letters which would cause toInt to throw e.g. 107a.
        (buildingNumberSanitised, addressDpa.houseName) // TODO check with BAs how they would want to sort the list
      })
    }

    def toDropDown(resp: Response): Seq[(String, String)] = {
      val addresses = extractFromJson(resp)
      sort(addresses) map { address => (address.presentation.uprn, address.toViewModel.mkString(", ")) }
      // Sort before translating to drop down format.
    }

    ws.callPostcodeWebService(postcode, trackingId).map {
      resp =>
        Logger.debug(s"Http response code from GDS postcode lookup service was: ${ resp.status }")
        if (resp.status == play.api.http.Status.OK) toDropDown(resp)
        else Seq.empty // The service returned http code other than 200 OK
    }.recover {
      case e: Throwable =>
        Logger.error(s"GDS postcode lookup service error: $e")
        Seq.empty
    }
  }

  override def fetchAddressForUprn(uprn: String, trackingId: String)
                                  (implicit lang: Lang): Future[Option[AddressViewModel]] = {
    def toViewModel(resp: Response) = {
      val addresses = extractFromJson(resp)
      require(addresses.length >= 1, s"Should be at least one address for the UPRN: $uprn")
      Some(AddressViewModel(uprn = Some(addresses.head.presentation.uprn.toLong), address = addresses.head.toViewModel))
      // Translate to view model.
    }

    ws.callUprnWebService(uprn, trackingId).map { resp =>
        Logger.debug(s"Http response code from GDS postcode lookup service was: ${ resp.status }")
        if (resp.status == play.api.http.Status.OK) toViewModel(resp)
        else None
    }.recover {
      case e: Throwable =>
        Logger.error(s"GDS uprn lookup service error: $e")
        None
    }
  }
}