package controllers.disposal_of_vehicle

import common.ClientSideSessionFactory
import services.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress
import services.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddressNotFound
import services.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddressNotFound
import helpers.UnitSpec
import mappings.disposal_of_vehicle.BusinessChooseYourAddress.AddressSelectId
import services.fakes.FakeAddressLookupWebServiceImpl
import utils.helpers.Config

class BusinessChooseYourAddressFormSpec extends UnitSpec {
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

  private def businessChooseYourAddressWithFakeWebService(uprnFound: Boolean = true) = {
    val responsePostcode = if (uprnFound) responseValidForPostcodeToAddress
                           else responseValidForPostcodeToAddressNotFound
    val responseUprn = if (uprnFound) responseValidForUprnToAddress else responseValidForUprnToAddressNotFound
    val fakeWebService = new FakeAddressLookupWebServiceImpl(responsePostcode, responseUprn)
    val addressLookupService = new services.address_lookup.ordnance_survey.AddressLookupServiceImpl(fakeWebService)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    new BusinessChooseYourAddress(addressLookupService)
  }

  private def formWithValidDefaults(addressSelected: String = traderUprnValid.toString) = {
    businessChooseYourAddressWithFakeWebService().form.bind(
      Map(AddressSelectId -> addressSelected)
    )
  }
}