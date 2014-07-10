package controllers.disposal_of_vehicle

import com.tzavellas.sse.guice.ScalaModule
import Common.ExitButtonHtml
import Common.PrototypeHtml
import common.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import controllers.disposal_of_vehicle
import services.fakes.FakeAddressLookupService
import services.fakes.FakeVehicleLookupWebService
import FakeAddressLookupService.{TraderBusinessNameValid, BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import FakeVehicleLookupWebService.ReferenceNumberValid
import FakeVehicleLookupWebService.RegistrationNumberValid
import FakeVehicleLookupWebService.RegistrationNumberWithSpaceValid
import FakeVehicleLookupWebService.vehicleDetailsNoResponse
import FakeVehicleLookupWebService.vehicleDetailsResponseDocRefNumberNotLatest
import FakeVehicleLookupWebService.vehicleDetailsResponseNotFoundResponseCode
import FakeVehicleLookupWebService.vehicleDetailsResponseSuccess
import FakeVehicleLookupWebService.vehicleDetailsResponseVRMNotFound
import FakeVehicleLookupWebService.vehicleDetailsServerDown
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.WithApplication
import mappings.common.DocumentReferenceNumber
import mappings.disposal_of_vehicle.Dispose.SurveyRequestTriggerDateCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.DocumentReferenceNumberId
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupFormModelCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupResponseCodeCacheKey
import mappings.disposal_of_vehicle.VehicleLookup.VehicleRegistrationNumberId
import models.domain.common.BruteForcePreventionViewModel
import BruteForcePreventionViewModel.BruteForcePreventionViewModelCacheKey
import models.domain.disposal_of_vehicle.VehicleLookupFormModel
import org.joda.time.Instant
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{when, verify}
import pages.disposal_of_vehicle.BeforeYouStartPage
import pages.disposal_of_vehicle.BusinessChooseYourAddressPage
import pages.disposal_of_vehicle.DisposePage
import pages.disposal_of_vehicle.EnterAddressManuallyPage
import pages.disposal_of_vehicle.MicroServiceErrorPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.VehicleLookupFailurePage
import pages.disposal_of_vehicle.VrmLockedPage
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.Response
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import services.brute_force_prevention.BruteForcePreventionService
import services.brute_force_prevention.BruteForcePreventionServiceImpl
import services.brute_force_prevention.BruteForcePreventionWebService
import services.DateServiceImpl
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.{FakeDateServiceImpl, FakeResponse}
import services.vehicle_lookup.{VehicleLookupServiceImpl, VehicleLookupWebService}
import utils.helpers.Config
import FakeBruteForcePreventionWebServiceImpl.{VrmLocked, VrmAttempt2, responseFirstAttempt, responseSecondAttempt, VrmThrows}
import models.domain.common.{VehicleDetailsResponse, VehicleDetailsRequest}

final class VehicleLookupUnitSpec extends UnitSpec {
  val testDuration = 7.days.toMillis
  implicit val dateService = new DateServiceImpl

  "present" should {
    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "redirect to setupTradeDetails page when user has not set up a trader for disposal" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator().present(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(ReferenceNumberValid)
      content should include(RegistrationNumberValid)
    }

    "not display exit button when DisposeOccurredCacheKey cookie is missing" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should not include ExitButtonHtml
    }

    "display exit button when DisposeOccurredCacheKey cookie is present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeOccurred)
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(ExitButtonHtml)
    }

    "display data captured in previous pages" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)

      content should include(TraderBusinessNameValid)
      content should include(BuildingNameOrNumberValid)
      content should include(Line2Valid)
      content should include(Line3Valid)
      content should include(PostTownValid)
      content should include(services.fakes.FakeAddressLookupService.PostcodeValid)
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator(isPrototypeBannerVisible = false).present(request)

      contentAsString(result) should not include PrototypeHtml
    }

    "offer the survey on first successful dispose" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)
      implicit val config = mockSurveyConfig()

      val vehiclesLookup = lookupWithMockConfig(config)

      contentAsString(vehiclesLookup.present(request)) should include(config.prototypeSurveyUrl)
    }

    "not offer the survey for one just after the initial survey offering" in new WithApplication {
      implicit val config = mockSurveyConfig()
      val aMomentAgo = (Instant.now.getMillis - 100).toString

      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(aMomentAgo))
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)

      val vehiclesLookup = lookupWithMockConfig(config)
      contentAsString(vehiclesLookup.present(request)) should not include config.prototypeSurveyUrl
    }

    "offer the survey one week after the first offering" in new WithApplication {
      implicit val config = mockSurveyConfig()

      val moreThen7daysAgo =
        (Instant.now.getMillis - config.prototypeSurveyPrepositionInterval - 1.minute.toMillis).toString
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(moreThen7daysAgo))
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)

      val vehiclesLookup = lookupWithMockConfig(config)
      contentAsString(vehiclesLookup.present(request)) should include(config.prototypeSurveyUrl)
    }

    "not offer the survey one week after the first offering" in new WithApplication {
      implicit val config = mockSurveyConfig()

      val lessThen7daysАgo =
        (Instant.now.getMillis - config.prototypeSurveyPrepositionInterval + 1.minute.toMillis).toString
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeSurveyUrl(lessThen7daysАgo))
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)

      val vehiclesLookup = lookupWithMockConfig(config)
      contentAsString(vehiclesLookup.present(request)) should not include config.prototypeSurveyUrl
    }

    "not offer the survey if the survey url is not set in the config" in new WithApplication {
      implicit val config: Config = mock[Config]
      when(config.prototypeSurveyUrl).thenReturn("")
      when(config.prototypeSurveyPrepositionInterval).thenReturn(testDuration)

      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.disposeOccurred)

      val vehiclesLookup = testInjector(new ScalaModule() {
        override def configure(): Unit = bind[Config].toInstance(config)
      }).getInstance(classOf[VehicleLookup])

      val result = vehiclesLookup.present(request)

      contentAsString(result) should not include "survey"
    }
  }

  "submit" should {
    "redirect to Dispose after a valid submit and true message returned from the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator().submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposePage.address))
        val cookies = fetchCookiesFromHeaders(r)
        val cookieName = "vehicleLookupFormModel"
        cookies.find(_.name == cookieName) match {
          case Some(cookie) =>
            val json = cookie.value
            val model = deserializeJsonToModel[VehicleLookupFormModel](json)
            model.registrationNumber should equal(RegistrationNumberValid.toUpperCase)
          case None => fail(s"$cookieName cookie not found")
        }
      }
    }

    "submit removes spaces from registrationNumber" in new WithApplication {
      // DE7 Spaces should be stripped
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
      val result = vehicleLookupResponseGenerator().submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(VehicleLookupFormModelCacheKey)
      }
    }

    "redirect to MicroServiceError after a submit and no response code and no vehicledetailsdto returned from the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseNotFoundResponseCode).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "redirect to VehicleLookupFailure after a submit and vrm not found by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseVRMNotFound).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to VehicleLookupFailure after a submit and document reference number mismatch returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseDocRefNumberNotLatest).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to VehicleLookupFailure after a submit and vss error returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsServerDown).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "return a bad request if dealer details are in cache and no details are entered" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "", registrationNumber = "").
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)

      result.futureValue.header.status should equal(play.api.http.Status.BAD_REQUEST)
    }

    "redirect to setupTradeDetails page if dealer details are not in cache and no details are entered" in new WithApplication {

      val request = buildCorrectlyPopulatedRequest(referenceNumber = "", registrationNumber = "")
      val result = vehicleLookupResponseGenerator().submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "replace max length error message for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1)).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "").
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX").
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "").
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of the page and once above the field.
    }

    "redirect to EnterAddressManually when back button is pressed and there is no uprn" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(EnterAddressManuallyPage.address))
    }

    "redirect to BusinessChooseYourAddress when back button is pressed and there is a uprn" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(uprn = Some(traderUprnValid)))
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to SetupTradeDetails page when back button is pressed and dealer details is not in cache" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "redirect to SetUpTradeDetails when back button and the user has completed the vehicle lookup form" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(uprn = Some(traderUprnValid)))
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to SetUpTradeDetails when back button clicked and there are no trader details stored in cache" in new WithApplication {
      // No cache setup with dealer details
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "redirect to MicroserviceError when microservice throws" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupError.submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError after a submit if response status is Ok and no response payload" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsNoResponse).submit(request)

      // TODO This test passes for the wrong reason, it is throwing when VehicleLookupServiceImpl tries to access resp.json, whereas we want VehicleLookupServiceImpl to return None as a response payload.
      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "write cookie when vss error returned by the microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsServerDown).submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(VehicleLookupFormModelCacheKey)
      }
    }

    "write cookie when document reference number mismatch returned by microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(fullResponse = vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BruteForcePreventionViewModelCacheKey, VehicleLookupResponseCodeCacheKey, VehicleLookupFormModelCacheKey)
      }
    }

    "write cookie when vrm not found by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(fullResponse = vehicleDetailsResponseVRMNotFound).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BruteForcePreventionViewModelCacheKey, VehicleLookupResponseCodeCacheKey, VehicleLookupFormModelCacheKey)
      }
    }

    "does not write cookie when microservice throws" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupError.submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
        val cookies = fetchCookiesFromHeaders(r)
        cookies shouldBe empty
      }
    }

    "redirect to vrm locked when valid submit and brute force prevention returns not permitted" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmLocked)
      val result = vehicleLookupResponseGenerator(
        vehicleDetailsResponseDocRefNumberNotLatest,
        bruteForceService = bruteForceServiceImpl(permitted = false)
      ).submit(request)
      result.futureValue.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
    }

    "redirect to VehicleLookupFailure and display 1st attempt message when document reference number not found and security service returns 1st attempt" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberValid)
      val result = vehicleLookupResponseGenerator(
        vehicleDetailsResponseDocRefNumberNotLatest,
        bruteForceService = bruteForceServiceImpl(permitted = true)
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to VehicleLookupFailure and display 2nd attempt message when document reference number not found and security service returns 2nd attempt" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmAttempt2)
      val result = vehicleLookupResponseGenerator(
        vehicleDetailsResponseDocRefNumberNotLatest,
        bruteForceService = bruteForceServiceImpl(permitted = true)
      ).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "Send a request and a trackingId" in new WithApplication {
      val trackingId = "x" * 20
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))
      val mockVehiclesLookupService = mock[VehicleLookupWebService]
      when(mockVehiclesLookupService.callVehicleLookupService(any[VehicleDetailsRequest], any[String])).
        thenReturn(Future {
          new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
        })
      val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(mockVehiclesLookupService)
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)

      val vehiclesLookup = new disposal_of_vehicle.VehicleLookup(
        bruteForceServiceImpl(permitted = true),
        vehicleLookupServiceImpl,
        surveyUrl = surveyUrl,
        dateService = dateService
      )
      val result = vehiclesLookup.submit(request)

      whenReady(result) { r =>
        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
        verify(mockVehiclesLookupService).callVehicleLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
        trackingIdCaptor.getValue should be(trackingId)
      }
    }

    "Send the request and no trackingId if session is not present" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val mockVehiclesLookupService = mock[VehicleLookupWebService]
      when(mockVehiclesLookupService.callVehicleLookupService(any[VehicleDetailsRequest], any[String])).thenReturn(Future {
        new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
      })
      val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(mockVehiclesLookupService)
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
      val vehiclesLookup = new disposal_of_vehicle.VehicleLookup(
        bruteForceServiceImpl(permitted = true),
        vehicleLookupServiceImpl,
        surveyUrl = surveyUrl,
        dateService = dateService
      )
      val result = vehiclesLookup.submit(request)

      whenReady(result) { r =>
        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
        verify(mockVehiclesLookupService).callVehicleLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
        trackingIdCaptor.getValue should be(ClearTextClientSideSessionFactory.DefaultTrackingId)
      }
    }

    "exit" should {
      "redirect to BeforeYouStartPage" in new WithApplication {
        val request = buildCorrectlyPopulatedRequest().
          withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        val mockVehiclesLookupService = mock[VehicleLookupWebService]
        val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(mockVehiclesLookupService)
        implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
        implicit val config: Config = mock[Config]
        implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
        val vehiclesLookup = new disposal_of_vehicle.VehicleLookup(
          bruteForceServiceImpl(permitted = true),
          vehicleLookupServiceImpl,
          surveyUrl = surveyUrl,
          dateService = dateService
        )
        val result = vehiclesLookup.exit(request)
        whenReady(result) { r =>
          r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
        }
      }

      "set the surveyRequestTriggerDate to the current date" in new WithApplication {
        val request = buildCorrectlyPopulatedRequest()
          .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
          .withCookies(CookieFactoryForUnitSpecs.preventGoingToDisposePage(""))
        val result = lookupWithMockConfig(mockSurveyConfig("http://www.google.com")).exit(request)
        whenReady(result) {r =>
          val cookies = fetchCookiesFromHeaders(r)
          val surveyTime = cookies.find(_.name == SurveyRequestTriggerDateCacheKey).get.value.toLong
          surveyTime should be <= System.currentTimeMillis()
          surveyTime should be > System.currentTimeMillis() - 1000
        }
      }
    }
  }

  private def responseThrows: Future[Response] = Future {
    throw new RuntimeException("This error is generated deliberately by a test")
  }

  private def bruteForceServiceImpl(permitted: Boolean): BruteForcePreventionService = {
    def bruteForcePreventionWebService: BruteForcePreventionWebService = {
      val status = if (permitted) play.api.http.Status.OK else play.api.http.Status.FORBIDDEN
      val bruteForcePreventionWebService: BruteForcePreventionWebService = mock[BruteForcePreventionWebService]

      when(bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid)).thenReturn(Future {
        new FakeResponse(status = status, fakeJson = responseFirstAttempt)
      })
      when(bruteForcePreventionWebService.callBruteForce(FakeBruteForcePreventionWebServiceImpl.VrmAttempt2)).
        thenReturn(Future {
          new FakeResponse(status = status, fakeJson = responseSecondAttempt)
        })
      when(bruteForcePreventionWebService.callBruteForce(FakeBruteForcePreventionWebServiceImpl.VrmLocked)).
        thenReturn(Future {
          new FakeResponse(status = status)
        })
      when(bruteForcePreventionWebService.callBruteForce(VrmThrows)).thenReturn(responseThrows)

      bruteForcePreventionWebService
    }

    new BruteForcePreventionServiceImpl(
      config = new Config(),
      ws = bruteForcePreventionWebService,
      dateService = new FakeDateServiceImpl
    )
  }

  private def vehicleLookupResponseGenerator(fullResponse: (Int, Option[VehicleDetailsResponse]) = vehicleDetailsResponseSuccess,
                                             bruteForceService: BruteForcePreventionService = bruteForceServiceImpl(permitted = true),
                                             isPrototypeBannerVisible: Boolean = true) = {
    val (status, vehicleDetailsResponse) = fullResponse
    val ws: VehicleLookupWebService = mock[VehicleLookupWebService]
    when(ws.callVehicleLookupService(any[VehicleDetailsRequest], any[String])).thenReturn(Future {
      val responseAsJson: Option[JsValue] = vehicleDetailsResponse match {
        case Some(e) => Some(Json.toJson(e))
        case _ => None
      }
      new FakeResponse(status = status, fakeJson = responseAsJson) // Any call to a webservice will always return this successful response.
    })
    val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(ws)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.prototypeSurveyUrl).thenReturn("http://fake/survey/url")
    new disposal_of_vehicle.VehicleLookup(
      bruteForceService = bruteForceService,
      vehicleLookupService = vehicleLookupServiceImpl,
      surveyUrl = surveyUrl,
      dateService = dateService
    )
  }

  private lazy val vehicleLookupError = {
    val permitted = true // The lookup is permitted as we want to test failure on the vehicle lookup micro-service step.
    val vehicleLookupWebService: VehicleLookupWebService = mock[VehicleLookupWebService]
    when(vehicleLookupWebService.callVehicleLookupService(any[VehicleDetailsRequest], any[String])).thenReturn(Future {
      throw new IllegalArgumentException
    })
    val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(vehicleLookupWebService)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
    new disposal_of_vehicle.VehicleLookup(
      bruteForceService = bruteForceServiceImpl(permitted = permitted),
      vehicleLookupService = vehicleLookupServiceImpl,
      surveyUrl = surveyUrl,
      dateService = dateService
    )
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber)
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
    vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).present(request)
  }

  def lookupWithMockConfig(config: Config): VehicleLookup =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[VehicleLookup])

  def mockSurveyConfig(url: String = "http://test/survey/url"): Config = {
    val config = mock[Config]
    val surveyUrl = url
    when(config.prototypeSurveyUrl).thenReturn(surveyUrl)
    when(config.prototypeSurveyPrepositionInterval).thenReturn(testDuration)
    config
  }
}