package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleAndKeeperDetailsModel
import common.webserviceclients.addresslookup.AddressLookupService

class AddressLookup @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              addressLookup: AddressLookupService)
                              extends common.controllers.AddressLookup {

  override protected def authenticate(request: Request[_]) =
    request.cookies.getModel[VehicleAndKeeperDetailsModel].fold(false)(m => true)
}