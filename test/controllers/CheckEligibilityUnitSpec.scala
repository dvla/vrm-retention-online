package controllers

import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.ErrorPage
import play.api.test.FakeRequest
import play.api.test.Helpers._

final class CheckEligibilityUnitSpec extends UnitSpec {

  "present" should {

    "redirect to error page when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
      val result = checkEligibility.present(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }
  }

  private lazy val checkEligibility = testInjector().getInstance(classOf[CheckEligibility])
}