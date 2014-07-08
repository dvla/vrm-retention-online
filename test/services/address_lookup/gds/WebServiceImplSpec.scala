package services.address_lookup.gds

import helpers.UnitSpec
import services.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeValidWithSpace}
import utils.helpers.Config

final class WebServiceImplSpec extends UnitSpec {
  "postcodeWithNoSpaces" should {
    "return the same string if no spaces present" in {
      val result = addressLookupService.postcodeWithNoSpaces(PostcodeValid)

      result should equal(PostcodeValid)
    }

    "remove spaces when present" in {
      val result = addressLookupService.postcodeWithNoSpaces(PostcodeValidWithSpace)

      result should equal(PostcodeValid)
    }
  }

  private val addressLookupService = new services.address_lookup.gds.WebServiceImpl(new Config())
}