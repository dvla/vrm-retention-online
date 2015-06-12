package controllers

import models.CacheKeyPrefix
import com.google.inject.Inject
import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.webserviceclients.addresslookup.AddressLookupService
import common.clientsidesession.CookieImplicits.RichCookies
import common.model.VehicleAndKeeperDetailsModel

class AddressLookup @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              addressLookup: AddressLookupService) extends common.controllers.AddressLookup {
  override protected def authenticate(request: Request[_]) =
    request.cookies.getModel[VehicleAndKeeperDetailsModel].fold(false)(m => true)
}
