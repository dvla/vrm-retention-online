package controllers.vrm_retention

import helpers.UnitSpec
import mappings.vrm_retention.BusinessChooseYourAddress.AddressSelectId
import services.fakes.FakeAddressLookupWebServiceImpl
import services.fakes.FakeAddressLookupWebServiceImpl.{responseValidForPostcodeToAddress, responseValidForPostcodeToAddressNotFound, responseValidForUprnToAddress, responseValidForUprnToAddressNotFound, traderUprnValid}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupWebService
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl

final class BusinessChooseYourAddressFormSpec extends UnitSpec {

  "form" should {

    "accept when all fields contain valid responses" in {
      formWithValidDefaults().get.uprnSelected should equal(traderUprnValid.toString)
    }
  }

  "addressSelect" should {

    "reject if empty" in {
      val errors = formWithValidDefaults(addressSelected = "").errors
      errors.length should equal(1)
      errors(0).key should equal(AddressSelectId)
      errors(0).message should equal("error.required")
    }
  }

  private def formWithValidDefaults(addressSelected: String = traderUprnValid.toString) = {
    businessChooseYourAddressWithFakeWebService().form.bind(
      Map(AddressSelectId -> addressSelected)
    )
  }

  private def businessChooseYourAddressWithFakeWebService(uprnFound: Boolean = true) = {
    val fakeWebService = injector.getInstance(classOf[AddressLookupWebService])
    val addressLookupService = new AddressLookupServiceImpl(fakeWebService)  /////// 
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    new BusinessChooseYourAddress(addressLookupService)
  }
}