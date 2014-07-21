package controllers.vrm_retention

import common.ClientSideSessionFactory
import controllers.vrm_retention.Common.PrototypeHtml
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import pages.vrm_retention.{BeforeYouStartPage, SetupBusinessDetailsPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import services.fakes.FakeDateServiceImpl
import utils.helpers.Config

final class VrmLockedUnitSpec extends UnitSpec {
  "present" should {
//    "display the page" in new WithApplication {
//      whenReady(present) { r =>
//        r.header.status should equal(play.api.http.Status.OK)
//      }
//    }

//    "display prototype message when config set to true" in new WithApplication {
//      contentAsString(present) should include(PrototypeHtml)
//    }

//    "not display prototype message when config set to false" in new WithApplication {
//      val request = FakeRequest()
//      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
//      implicit val config: Config = mock[Config]
//      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
//      val vrmLockedPrototypeNotVisible = new VrmLocked()
//
//      val result = vrmLockedPrototypeNotVisible.present(request)
//      contentAsString(result) should not include PrototypeHtml
//    }
  }

  "exit" should {
    "redirect to correct next page after the exit button is clicked" in new WithApplication {
      val request = FakeRequest()
      val result = vrmLocked.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private val vrmLocked = injector.getInstance(classOf[VrmLocked])

  private lazy val present = {
    val dateService = new FakeDateServiceImpl
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.bruteForcePreventionViewModel(
        dateTimeISOChronology = dateService.dateTimeISOChronology)
      )
    vrmLocked.present(request)
  }
}