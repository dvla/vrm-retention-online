package controllers

import composition.WithApplication
import helpers.UnitSpec
import pages.vrm_retention.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers._

final class ApplicationUnitSpec extends UnitSpec {

  "index" should {

    "redirect to the before-you-start page" in new WithApplication {
      val result = application.index(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private lazy val application = testInjector().getInstance(classOf[Application])
}