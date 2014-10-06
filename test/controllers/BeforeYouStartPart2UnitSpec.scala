package controllers

import composition.TestConfig
import controllers.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.BeforeYouStartPart2Page
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout, status}

final class BeforeYouStartPart2UnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val result = beforeYouStartPart2.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(BeforeYouStartPart2Page.title)
    }

    "display prototype message when config set to true" in new WithApplication {
      val result = beforeYouStartPart2.present(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = beforeYouStartPart2PrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private lazy val beforeYouStartPart2PrototypeNotVisible = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false)).
      getInstance(classOf[BeforeYouStartPart2])
  }

  private lazy val beforeYouStartPart2 = testInjector().getInstance(classOf[BeforeYouStartPart2])
}