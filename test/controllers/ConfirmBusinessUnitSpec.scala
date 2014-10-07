package controllers

import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import play.api.test.FakeRequest
import play.api.test.Helpers.OK

final class ConfirmBusinessUnitSpec extends UnitSpec {

  "present" should {

    "display the page when required cookies are cached" in new WithApplication {
      whenReady(present, timeout) { r =>
        r.header.status should equal(OK)
      }
    }
  }

  private def present = {
    val request = FakeRequest().
      withCookies(
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel(),
        businessDetailsModel()
      )
    confirmBusiness.present(request)
  }

  private def confirmBusiness = testInjector().getInstance(classOf[ConfirmBusiness])
}