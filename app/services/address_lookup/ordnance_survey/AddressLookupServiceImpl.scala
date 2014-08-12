package services.address_lookup.ordnance_survey

import javax.inject.Inject

import common.LogFormats
import play.api.Logger
import play.api.i18n.Lang
import play.api.libs.ws.Response
import services.address_lookup.{AddressLookupService, AddressLookupWebService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.domain.common.AddressViewModel
import models.domain.vrm_retention.{UprnToAddressResponse, PostcodeToAddressResponse}

final class AddressLookupServiceImpl @Inject()(ws: AddressLookupWebService) extends AddressLookupService {

  override def fetchAddressesForPostcode(postcode: String, trackingId: String)
                                        (implicit lang: Lang): Future[Seq[(String, String)]] = {

    def extractFromJson(resp: Response): Option[PostcodeToAddressResponse] =
      resp.json.asOpt[PostcodeToAddressResponse]

    def toDropDown(resp: Response): Seq[(String, String)] =
      extractFromJson(resp) match {
        case Some(results) =>
          results.addresses.map(address => (address.uprn, address.address))
        case None =>
          // Handle no results
          val postcodeToLog = LogFormats.anonymize(postcode)
          Logger.debug(s"No results returned for postcode: $postcodeToLog")
          Seq.empty// Exception case and empty seq case are treated the same in the UI
      }

    ws.callPostcodeWebService(postcode, trackingId)(lang).map { resp =>
        Logger.debug(s"Http response code from Ordnance Survey postcode lookup service was: ${resp.status}")
        if (resp.status == play.api.http.Status.OK) toDropDown(resp)
        else Seq.empty // The service returned http code other than 200 OK
    }.recover {
      case e: Throwable =>
        Logger.error(s"Ordnance Survey postcode lookup service error.", e)
        Seq.empty // Exception case and empty seq case are treated the same in the UI
    }
  }

  override def fetchAddressForUprn(uprn: String, trackingId: String)
                                  (implicit lang: Lang): Future[Option[AddressViewModel]] = {

    // Extract result from response and return as a view model.
    def extractFromJson(resp: Response): Option[UprnToAddressResponse] = {
      resp.json.asOpt[UprnToAddressResponse]
    }

    def toViewModel(resp: Response) =
      extractFromJson(resp) match {
        case Some(deserialized) => deserialized.addressViewModel
        case None =>
          val uprnToLog = LogFormats.anonymize(uprn)
          Logger.error(s"Could not deserialize response of web service for submitted UPRN: $uprnToLog")
          None
      }

    ws.callUprnWebService(uprn, trackingId).map { resp =>
        Logger.debug(s"Http response code from Ordnance Survey uprn lookup service was: ${resp.status}")
        if (resp.status == play.api.http.Status.OK) toViewModel(resp)
        else None
    }.recover {
      case e: Throwable =>
        Logger.error(s"Ordnance Survey postcode lookup service error", e)
        None
    }
  }
}