package controllers

import composition.{TestConfig, WithApplication}
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.{SERVICE_UNAVAILABLE, contentAsString, defaultAwaitTimeout, status}

final class MicroserviceErrorUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      status(present) should equal(SERVICE_UNAVAILABLE)
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = microServiceErrorPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private def microServiceErrorPrototypeNotVisible = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false)).
      getInstance(classOf[MicroServiceError])
  }

  private lazy val present = microServiceError.present(FakeRequest())
  private lazy val microServiceError = testInjector().getInstance(classOf[MicroServiceError])
}
