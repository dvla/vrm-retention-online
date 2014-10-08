package controllers

import composition.TestConfig
import controllers.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout, status}

final class BeforeYouStartUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val result = beforeYouStart.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(BeforeYouStartPage.title)
    }

    "display prototype message when config set to true" in new WithApplication {
      val result = beforeYouStart.present(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = beforeYouStartPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private lazy val beforeYouStartPrototypeNotVisible = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false)).
      getInstance(classOf[BeforeYouStart])
  }

  private lazy val beforeYouStart = testInjector().getInstance(classOf[BeforeYouStart])
}