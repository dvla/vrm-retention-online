package controllers.disposal_of_vehicle

import common.ClientSideSessionFactory
import helpers.{WithApplication, UnitSpec}
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import org.mockito.Mockito.when
import services.fakes.FakeDateServiceImpl
import play.api.test.Helpers._
import utils.helpers.Config
import scala.Some
import pages.disposal_of_vehicle.{BeforeYouStartPage, VehicleLookupPage, SetupTradeDetailsPage}
import play.api.test.FakeRequest

final class VrmLockedUnitSpec extends UnitSpec {
  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) {
        r => r.header.status should equal(play.api.http.Status.OK)
      }
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include("""<div class="prototype">""")
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val vrmLockedPrototypeNotVisible = new VrmLocked()

      val result = vrmLockedPrototypeNotVisible.present(request)
      contentAsString(result) should not include """<div class="prototype">"""
    }
  }

  "newDisposal" should {
    "redirect to vehicle lookup page after the new disposal button is clicked when the expected data is in the cookies" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vrmLocked.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to setup trade details page after the new disposal button is clicked when the expected data is not in the cookies" in new WithApplication {
      val request = FakeRequest()
      val result = vrmLocked.newDisposal(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  "exit" should {
    "redirect to correct next page after the exit button is clicked" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vrmLocked.exit(request)
      whenReady(result) {
        r => r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private val vrmLocked = injector.getInstance(classOf[VrmLocked])
  private lazy val present = {
    val dateService = new FakeDateServiceImpl
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.bruteForcePreventionViewModel(dateTimeISOChronology = dateService.dateTimeISOChronology))
    vrmLocked.present(request)
  }
}
