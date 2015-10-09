package composition.webserviceclients.addresslookup

import _root_.webserviceclients.fakes.AddressLookupWebServiceConstants
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Matchers.matches
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Lang
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupWebService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl

final class TestAddressLookupBinding extends ScalaModule with MockitoSugar {

  def configure() = {
    bind[AddressLookupService].to[AddressLookupServiceImpl]

    val stubbedWebServiceImpl = mock[AddressLookupWebService]
    when(stubbedWebServiceImpl.callPostcodeWebService(
      matches(AddressLookupWebServiceConstants.traderUprnValid.toString),
      trackingId = any[TrackingId]
    )(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddress)
    when(stubbedWebServiceImpl.callPostcodeWebService(
      matches(AddressLookupWebServiceConstants.traderUprnInvalid.toString),
      trackingId = any[TrackingId]
    )(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddressNotFound)

    bind[AddressLookupWebService].toInstance(stubbedWebServiceImpl)
  }
}