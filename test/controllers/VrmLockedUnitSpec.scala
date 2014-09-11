package controllers

import com.tzavellas.sse.guice.ScalaModule
import controllers.Common.PrototypeHtml
import helpers.vrm_retention.CookieFactoryForUnitSpecs.{bruteForcePreventionViewModel, transactionId, vehicleAndKeeperDetailsModel, vehicleAndKeeperLookupFormModel}
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import pages.vrm_retention.MockFeedbackPage
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config

final class VrmLockedUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = vrmLockedPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "exit" should {

    "redirect to correct next page after the exit button is clicked" in new WithApplication {
      val request = FakeRequest()
      val result = vrmLocked.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }
  }

  private lazy val present = {
    val dateService = injector.getInstance(classOf[DateService])
    val request = FakeRequest().
      withCookies(transactionId()).
      withCookies(bruteForcePreventionViewModel(dateTimeISOChronology = dateService.dateTimeISOChronology)).
      withCookies(vehicleAndKeeperLookupFormModel()).
      withCookies(vehicleAndKeeperDetailsModel())
    vrmLocked.present(request)
  }
  private lazy val vrmLocked = testInjectorOverrideDev().getInstance(classOf[VrmLocked])

  private def vrmLockedPrototypeNotVisible = {
    testInjectorOverrideDev(new ScalaModule() {
      override def configure(): Unit = {
        val config: Config = mock[Config]
        when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
        bind[Config].toInstance(config)
      }
    }).getInstance(classOf[VrmLocked])
  }
}