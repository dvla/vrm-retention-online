package controllers.vrm_retention

import common.ClientSideSessionFactory
import helpers.UnitSpec
import mappings.vrm_retention.BusinessChooseYourAddress.AddressSelectId
import services.fakes.FakeAddressLookupWebServiceImpl
import services.fakes.FakeAddressLookupWebServiceImpl.{responseValidForPostcodeToAddress, responseValidForPostcodeToAddressNotFound, responseValidForUprnToAddress, responseValidForUprnToAddressNotFound, traderUprnValid}
import utils.helpers.Config

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