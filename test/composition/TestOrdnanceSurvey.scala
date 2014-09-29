package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.{any, _}
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Lang
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeInvalid
import webserviceclients.fakes.AddressLookupWebServiceConstants

class TestOrdnanceSurvey extends ScalaModule with MockitoSugar {

  def configure() = {
    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]

    val stubbedWebServiceImpl = mock[AddressLookupWebService]
    when(stubbedWebServiceImpl.callPostcodeWebService(postcode = any[String], trackingId = any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForPostcodeToAddress)
    when(stubbedWebServiceImpl.callPostcodeWebService(matches(PostcodeInvalid.toUpperCase), any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseWhenPostcodeInvalid)
    when(stubbedWebServiceImpl.callUprnWebService(uprn = matches(AddressLookupWebServiceConstants.traderUprnValid.toString), trackingId = any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddress)
    when(stubbedWebServiceImpl.callUprnWebService(uprn = matches(AddressLookupWebServiceConstants.traderUprnInvalid.toString), trackingId = any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddressNotFound)

    bind[AddressLookupWebService].toInstance(stubbedWebServiceImpl)
  }
}
