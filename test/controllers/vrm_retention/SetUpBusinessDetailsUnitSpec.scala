package controllers.vrm_retention

import controllers.vrm_retention.Common.PrototypeHtml
import helpers.JsonUtils.deserializeJsonToModel
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import mappings.vrm_retention.SetupBusinessDetails._
import models.domain.vrm_retention.SetupBusinessDetailsFormModel
import org.mockito.Mockito.when
import pages.vrm_retention.BusinessChooseYourAddressPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import services.fakes.AddressLookupServiceConstants._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class SetUpBusinessDetailsUnitSpec extends UnitSpec {

  "present" should {

    //    "display the page" in new WithApplication {
    //      whenReady(present) { r =>
    //        r.header.status should equal(OK)
    //      }
    //    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails.present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include(PostcodeValid)
    }

    "display empty fields when setupBusinessDetails cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails.present(request)
      val content = contentAsString(result)
      content should not include TraderBusinessNameValid
      content should not include PostcodeValid
    }

    //    "display prototype message when config set to true" in new WithApplication {
    //      contentAsString(present) should include(PrototypeHtml)
    //    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false)
      // Stub this config value.
      val setUpTradeDetailsPrototypeNotVisible = new SetUpBusinessDetails()

      val result = setUpTradeDetailsPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {

    "redirect to next page when the form is completed successfully" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = setUpBusinessDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          val cookieName = "setupBusinessDetails"
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[SetupBusinessDetailsFormModel](json)
              model.businessName should equal(TraderBusinessNameValid.toUpperCase)
              model.businessPostcode should equal(PostcodeValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    //    "return a bad request if no details are entered" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest(dealerName = "", dealerPostcode = "")
    //      val result = setUpBusinessDetails.submit(request)
    //      whenReady(result) { r =>
    //        r.header.status should equal(BAD_REQUEST)
    //      }
    //    }
    //
    //    "replace max length error message for traderBusinessName with standard error message (US158)" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest(dealerName = "a" * (BusinessNameMaxLength + 1))
    //      val result = setUpBusinessDetails.submit(request)
    //      val count = "Must be between 2 and 58 characters and only contain valid characters".
    //        r.findAllIn(contentAsString(result)).length
    //      count should equal(2)
    //    }
    //
    //    "replace required and min length error messages for traderBusinessName with standard error message (US158)" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest(dealerName = "")
    //      val result = setUpBusinessDetails.submit(request)
    //      val count = "Must be between 2 and 58 characters and only contain valid characters".
    //        r.findAllIn(contentAsString(result)).length
    //      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of
    //      // the page and once above the field.
    //    }

    "write cookie when the form is completed successfully" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = setUpBusinessDetails.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(SetupBusinessDetailsCacheKey)
      }
    }
  }

  private lazy val present = {
    val request = FakeRequest()
    setUpBusinessDetails.present(request)
  }
  private val setUpBusinessDetails = injector.getInstance(classOf[SetUpBusinessDetails])

  private def buildCorrectlyPopulatedRequest(dealerName: String = TraderBusinessNameValid,
                                             dealerContact: String = TraderBusinessContactValid,
                                             dealerEmail: String = TraderBusinessEmailValid,
                                             dealerPostcode: String = PostcodeValid) = {
    FakeRequest().withFormUrlEncodedBody(
      BusinessNameId -> dealerName,
      BusinessContactId -> dealerContact,
      BusinessEmailId -> dealerEmail,
      BusinessPostcodeId -> dealerPostcode)
  }
}