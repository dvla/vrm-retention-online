package controllers

import controllers.Common.PrototypeHtml
import composition.TestConfig
import helpers.TestWithApplication
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.bruteForcePreventionViewModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import pages.vrm_retention.LeaveFeedbackPage
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import uk.gov.dvla.vehicles.presentation.common.services.DateService

class VrmLockedUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      val result = vrmLockedPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "exit" should {
    "redirect to correct next page after the exit button is clicked" in new TestWithApplication {
      val request = FakeRequest()
      val result = vrmLocked.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  private def present = {
    val dateService = testInjector().getInstance(classOf[DateService])
    val request = FakeRequest().
      withCookies(transactionId()).
      withCookies(bruteForcePreventionViewModel(dateTimeISOChronology = dateService.dateTimeISOChronology)).
      withCookies(vehicleAndKeeperLookupFormModel()).
      withCookies(vehicleAndKeeperDetailsModel())
    vrmLocked.present(request)
  }

  private def vrmLocked = testInjector().getInstance(classOf[VrmLocked])

  private def vrmLockedPrototypeNotVisible = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false)
    ).getInstance(classOf[VrmLocked])
  }
}