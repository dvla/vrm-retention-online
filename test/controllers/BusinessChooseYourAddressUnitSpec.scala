package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import composition.{TestConfig, TestOrdnanceSurvey}
import controllers.Common.PrototypeHtml
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{ConfirmBusinessPage, SetupBusinessDetailsPage, UprnNotFoundPage}
import play.api.mvc.Cookies
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, SET_COOKIE, contentAsString, _}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{CookieFlags, NoCookieFlags}
import views.vrm_retention.BusinessChooseYourAddress.{AddressSelectId, BusinessChooseYourAddressCacheKey}
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import webserviceclients.fakes.AddressLookupWebServiceConstants
import webserviceclients.fakes.AddressLookupWebServiceConstants.{traderUprnInvalid, traderUprnValid}
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperFirstNameValid

final class BusinessChooseYourAddressUnitSpec extends UnitSpec {

  "present" should {

    "display the page if dealer details cached" in new WithApplication {
      whenReady(present, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.present(request)
      val content = contentAsString(result)
      // HACK for Northern Ireland
//      content should include( s"""<option value="$traderUprnValid" selected>""")
      content should include( s"""<option value="0" selected>""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present)
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no business details cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddressWithPrototypeBannerNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {

    "redirect to Confirm page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }

    "return a bad request if not address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(traderUprn = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to SetupBusinessDetailsPage page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to SetupBusinessDetailsPage page when bad form submitted and no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(traderUprn = "")
      // Bad form because nothing was selected from the drop-down.
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(traderUprn = traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(BusinessChooseYourAddressCacheKey,
          BusinessDetailsCacheKey,
          EnterAddressManuallyCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(traderUprn = AddressLookupWebServiceConstants.traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, BusinessDetailsCacheKey)
      }
    }
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    businessChooseYourAddress.present(request)
  }

  private def businessChooseYourAddress = {
    testInjector(
      new TestOrdnanceSurvey,
      new TestVehicleAndKeeperLookupWebService,
      new ScalaModule() {
        override def configure(): Unit = {
          bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
        }
      }).getInstance(classOf[BusinessChooseYourAddress])
  }

  private def businessChooseYourAddressWithPrototypeBannerNotVisible = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false)).
      getInstance(classOf[BusinessChooseYourAddress])
  }

  // HACK for Norther Ireland
//  private def buildCorrectlyPopulatedRequest(traderUprn: String = traderUprnValid.toString) = {
  private def buildCorrectlyPopulatedRequest(traderUprn: String = "0") = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> traderUprn)
  }
}