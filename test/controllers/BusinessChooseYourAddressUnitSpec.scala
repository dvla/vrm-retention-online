package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import composition.{TestDateService, TestAuditService, TestConfig, TestOrdnanceSurvey}
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
import audit.{AuditMessage, Message, AuditService}
import org.mockito.Mockito._
import scala.Some

final class BusinessChooseYourAddressUnitSpec extends UnitSpec {

  "present (use UPRN enabled)" should {

    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = true), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$traderUprnValid" >""")
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddressUseUprn()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).present(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$traderUprnValid" selected>""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = true))
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no business details cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = true)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(isPrototypeBannerVisible = false, ordnanceSurveyUseUprn = true).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "present (use UPRN not enabled for Northern Ireland)" should {

    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = false), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).present(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" selected>""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = false))
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no business details cached" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = false)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(isPrototypeBannerVisible = false, ordnanceSurveyUseUprn = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit (use UPRN enabled)" should {

    "redirect to Confirm Business page after a valid submit" in new WithApplication {
      val mockAuditService = mock[AuditService]

      val businessChooseYourAddress = testInjector(
        new TestOrdnanceSurvey,
        new TestVehicleAndKeeperLookupWebService,
        new ScalaModule() {
          override def configure(): Unit = {
            bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
          }
        },
        new TestConfig(isPrototypeBannerVisible = true, ordnanceSurveyUseUprn = true),
        new TestAuditService(mockAuditService),
        new TestDateService).getInstance(classOf[BusinessChooseYourAddress])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", "1970-11-25T00:00:00.000+01:00"),
        ("replacementVrm", "SA11AA"),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName","Mr David Jones"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress", "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@email.com"))
      val auditMessage = new Message(AuditMessage.CaptureActorToConfirmBusiness, AuditMessage.PersonalisedRegServiceType, data: _*)
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnValid.toString).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel()).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
        verify(mockAuditService).send(auditMessage)
      }
    }

    "return a bad request if not address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$traderUprnValid" >""")
    }

    "redirect to SetupBusinessDetailsPage page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnValid.toString).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to SetupBusinessDetailsPage page when bad form submitted and no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      // Bad form because nothing was selected from the drop-down.
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnValid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(BusinessChooseYourAddressCacheKey,
          BusinessDetailsCacheKey,
          EnterAddressManuallyCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = AddressLookupWebServiceConstants.traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, BusinessDetailsCacheKey)
      }
    }
  }

  "submit (use UPRN not enabled for Northern Ireland)" should {

    "redirect to Confirm page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }

    "return a bad request if not address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "redirect to SetupBusinessDetailsPage page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to SetupBusinessDetailsPage page when bad form submitted and no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      // Bad form because nothing was selected from the drop-down.
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.eligibilityModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(BusinessChooseYourAddressCacheKey,
          BusinessDetailsCacheKey,
          EnterAddressManuallyCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = AddressLookupWebServiceConstants.traderUprnInvalid.toString).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails())
      val result = businessChooseYourAddress(ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, BusinessDetailsCacheKey)
      }
    }
  }

  private def present(ordnanceSurveyUseUprn: Boolean) = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    businessChooseYourAddress(ordnanceSurveyUseUprn = ordnanceSurveyUseUprn).present(request)
  }

  private def businessChooseYourAddress(isPrototypeBannerVisible: Boolean = true, ordnanceSurveyUseUprn: Boolean) = {
    testInjector(
      new TestOrdnanceSurvey,
      new TestVehicleAndKeeperLookupWebService,
      new ScalaModule() {
        override def configure(): Unit = {
          bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
        }
      },
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible, ordnanceSurveyUseUprn = ordnanceSurveyUseUprn),
      new TestAuditService
    ).getInstance(classOf[BusinessChooseYourAddress])
  }

  private def buildCorrectlyPopulatedRequest(addressSelected: String = "0") = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> addressSelected)
  }
}