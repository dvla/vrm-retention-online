package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.services.{DateService, DateServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupWebService, AddressLookupService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.{WebServiceImpl, AddressLookupServiceImpl}

final class AddressServiceBinding extends ScalaModule {

  def configure() = {
    bind[AddressLookupService].to[AddressLookupServiceImpl].asEagerSingleton()
    bind[AddressLookupWebService].to[WebServiceImpl].asEagerSingleton()
  }
}