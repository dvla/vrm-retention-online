package controllers

import com.tzavellas.sse.guice.ScalaModule
import controllers.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import pages.vrm_retention.VehicleLookupPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout, status}
import utils.helpers.Config

final class BeforeYouStartUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val result = beforeYouStart.present(FakeRequest())
      status(result) should equal(OK)
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

  "submit" should {

    "redirect to next page after the button is clicked" in new WithApplication {
      val result = beforeYouStart.submit(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  private lazy val beforeYouStartPrototypeNotVisible = {
    testInjectorOverrideDev(new ScalaModule() {
      override def configure(): Unit = {
        val config: Config = mock[Config]
        when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
        bind[Config].toInstance(config)
      }
    }).getInstance(classOf[BeforeYouStart])
  }

  private lazy val beforeYouStart = testInjectorOverrideDev().getInstance(classOf[BeforeYouStart])
}