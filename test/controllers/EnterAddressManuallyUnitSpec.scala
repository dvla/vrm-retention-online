package controllers

import composition.TestConfig
import controllers.Common.PrototypeHtml
import helpers.JsonUtils.deserializeJsonToModel
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import models.EnterAddressManuallyModel.Form.AddressAndPostcodeId
import models.{BusinessDetailsModel, EnterAddressManuallyModel}
import pages.vrm_retention.{ConfirmBusinessPage, ConfirmPage, SetupBusinessDetailsPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, contentAsString, defaultAwaitTimeout}
import webserviceclients.fakes.AddressLookupServiceConstants.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, PostcodeValid}
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form._
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.EnterAddressManually.{EnterAddressManuallyCacheKey, PostcodeId}
import scala.concurrent.Future

final class EnterAddressManuallyUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to SetupTraderDetails page when present with no dealer name cached" in new WithApplication {
      val request = FakeRequest()
      val result = enterAddressManually.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = enterAddressManually.present(request)
      val content = contentAsString(result)
      content should include(BuildingNameOrNumberValid)
      content should include(Line2Valid)
      content should include(Line3Valid)
      content should include(PostTownValid)
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val content = contentAsString(present)
      content should not include BuildingNameOrNumberValid
      content should not include Line2Valid
      content should not include Line3Valid
      content should not include PostTownValid
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      // TODO use testInjectory to override config
      val request = FakeRequest()
      val result = enterAddressManuallyPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {

    "return bad request when no data is entered" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())

      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return bad request a valid postcode is entered without an address" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to ConfirmBusiness page after a valid submission of all fields" in new WithApplication {
      val request = requestWithValidDefaults()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }

    "redirect to ConfirmBusiness after a valid submission of mandatory fields" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }

    "submit removes commas and full stops from the end of each address line" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house,",
        line2 = "my street.",
        line3 = "my area.",
        postTown = "my town,"
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE",
        line2 = "MY STREET",
        line3 = "MY AREA",
        postTown = "MY TOWN"
      )
    }

    "submit removes multiple commas and full stops from the end of each address line" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house,.,..,,",
        line2 = "my street...,,.,",
        line3 = "my area.,,..",
        postTown = "my town,,,.,,,."
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE",
        line2 = "MY STREET",
        line3 = "MY AREA",
        postTown = "MY TOWN"
      )
    }

    "submit does not remove multiple commas and full stops from the middle of address lines" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house 1.1,",
        line2 = "st. something street",
        line3 = "st. johns",
        postTown = "my t.own"
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE 1.1",
        line2 = "ST. SOMETHING STREET",
        line3 = "ST. JOHNS",
        postTown = "MY T.OWN"
      )
    }

    "submit removes commas, but still applies the min length rule" in new WithApplication {
      uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.trimNonWhiteListedChars( """[A-Za-z0-9\-]""")(",, m...,,,,   ") should equal("m")
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "m...,,,,   " // This should be a min length of 4 chars
      ))
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "submit does not accept an address containing only full stops" in new WithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "...")
      )

      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to SetupTraderDetails page when valid submit with no dealer name cached" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to setupBusinessDetails page when bad submit with no dealer name cached" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "collapse error messages for buildingNameOrNumber" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> "",
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = enterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Building name or number - Must contain a minimum of four characters")
    }

    "collapse error messages for post town" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> "",
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = enterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Post town - Requires a minimum length of three characters")
    }

    "write cookie after a valid submission of all fields" in new WithApplication {
      val request = requestWithValidDefaults()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(_.name == EnterAddressManuallyCacheKey) match {
          case Some(cookie) =>
            val json = cookie.value
            val model = deserializeJsonToModel[EnterAddressManuallyModel](json)

            model.addressAndPostcodeViewModel.addressLinesModel.buildingNameOrNumber should equal(
              BuildingNameOrNumberValid.toUpperCase)
            model.addressAndPostcodeViewModel.addressLinesModel.line2 should equal(Some(Line2Valid.toUpperCase))
            model.addressAndPostcodeViewModel.addressLinesModel.line3 should equal(Some(Line3Valid.toUpperCase))
            model.addressAndPostcodeViewModel.addressLinesModel.postTown should equal(PostTownValid.toUpperCase)
          case None => fail(s"$EnterAddressManuallyCacheKey cookie not found")
        }

        cookies.find(_.name == BusinessDetailsCacheKey) match {
          case Some(cookie) =>
            val json = cookie.value
            val model = deserializeJsonToModel[BusinessDetailsModel](json)
            val expectedData = Seq(BuildingNameOrNumberValid.toUpperCase,
              Line2Valid.toUpperCase,
              Line3Valid.toUpperCase,
              PostTownValid.toUpperCase,
              formatPostcode(PostcodeValid.toUpperCase))
            expectedData should equal(model.address.address)

          case None => fail(s"$BusinessDetailsCacheKey cookie not found")
        }
      }
    }
  }

  private def enterAddressManuallyPrototypeNotVisible = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false))
      .getInstance(classOf[EnterAddressManually])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    enterAddressManually.present(request)
  }

  private def enterAddressManually = testInjector().getInstance(classOf[EnterAddressManually])

  private def validateAddressCookieValues(result: Future[Result], buildingName: String, line2: String,
                                          line3: String, postTown: String, postCode: String = PostcodeValid) = {

    whenReady(result) { r =>
      val cookies = fetchCookiesFromHeaders(r)
      cookies.find(_.name == BusinessDetailsCacheKey) match {
        case Some(cookie) =>
          val json = cookie.value
          val model = deserializeJsonToModel[BusinessDetailsModel](json)
          val expectedData = Seq(buildingName,
            line2,
            line3,
            postTown,
            formatPostcode(postCode))
          expectedData should equal(model.address.address)
        case None => fail(s"$BusinessDetailsCacheKey cookie not found")
      }
    }
  }

  private def requestWithValidDefaults(buildingName: String = BuildingNameOrNumberValid,
                                       line2: String = Line2Valid,
                                       line3: String = Line3Valid,
                                       postTown: String = PostTownValid,
                                       postCode: String = PostcodeValid) =

    FakeRequest().withFormUrlEncodedBody(
      s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> buildingName,
      s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> line2,
      s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> line3,
      s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> postTown,
      s"$AddressAndPostcodeId.$PostcodeId" -> postCode).
      withCookies(CookieFactoryForUnitSpecs.setupBusinessDetails()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
}
