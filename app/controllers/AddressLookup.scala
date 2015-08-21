package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService

class AddressLookup @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              addressLookup: AddressLookupService)
                              extends uk.gov.dvla.vehicles.presentation.common.controllers.AddressLookup {

  override protected def authenticate(request: Request[_]) =
    request.cookies.getModel[VehicleAndKeeperDetailsModel].fold(false)(m => true)
}