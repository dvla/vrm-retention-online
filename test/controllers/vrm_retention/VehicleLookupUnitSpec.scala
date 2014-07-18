package controllers.vrm_retention

import com.tzavellas.sse.guice.ScalaModule
import controllers.vrm_retention.Common.PrototypeHtml
import common.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import controllers.vrm_retention
import services.fakes.FakeAddressLookupService._
import services.fakes.FakeVehicleLookupWebService.ReferenceNumberValid
import services.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import services.fakes.FakeVehicleLookupWebService.RegistrationNumberWithSpaceValid
import services.fakes.FakeVehicleLookupWebService.vehicleDetailsNoResponse
import services.fakes.FakeVehicleLookupWebService.vehicleDetailsResponseDocRefNumberNotLatest
import services.fakes.FakeVehicleLookupWebService.vehicleDetailsResponseNotFoundResponseCode
import services.fakes.FakeVehicleLookupWebService.vehicleDetailsResponseSuccess
import services.fakes.FakeVehicleLookupWebService.vehicleDetailsResponseVRMNotFound
import services.fakes.FakeVehicleLookupWebService.vehicleDetailsServerDown
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.WithApplication
import mappings.common.DocumentReferenceNumber
import mappings.vrm_retention.VehicleLookup.DocumentReferenceNumberId
import mappings.vrm_retention.VehicleLookup.VehicleLookupFormModelCacheKey
import mappings.vrm_retention.VehicleLookup.VehicleLookupResponseCodeCacheKey
import mappings.vrm_retention.VehicleLookup.VehicleRegistrationNumberId
import models.domain.common.BruteForcePreventionViewModel
import BruteForcePreventionViewModel.BruteForcePreventionViewModelCacheKey
import models.domain.vrm_retention.VehicleLookupFormModel
import org.joda.time.Instant
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{when, verify}
import pages.vrm_retention._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.Response
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import scala.concurrent.duration.DurationInt
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
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
import scala.Some
import play.api.libs.ws.Response

final class VehicleLookupUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(ReferenceNumberValid)
      content should include(RegistrationNumberValid)
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleLookupResponseGenerator(isPrototypeBannerVisible = false).present(request)

      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
//    "redirect to Confirm after a valid submit and true message returned from the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator().submit(request)
//
//      whenReady(result, timeout) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
//        val cookies = fetchCookiesFromHeaders(r)
//        val cookieName = "vehicleLookupFormModel"
//        cookies.find(_.name == cookieName) match {
//          case Some(cookie) =>
//            val json = cookie.value
//            val model = deserializeJsonToModel[VehicleLookupFormModel](json)
//            model.registrationNumber should equal(RegistrationNumberValid.toUpperCase)
//          case None => fail(s"$cookieName cookie not found")
//        }
//      }
//    }
//
//    "submit removes spaces from registrationNumber" in new WithApplication {
//      // DE7 Spaces should be stripped
//      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
//      val result = vehicleLookupResponseGenerator().submit(request)
//
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain(VehicleLookupFormModelCacheKey)
//      }
//    }
//
//    "redirect to MicroServiceError after a submit and no response code and no vehicledetailsdto returned from the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseNotFoundResponseCode).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
//    }
//
//    "redirect to VehicleLookupFailure after a submit and vrm not found by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseVRMNotFound).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
//    }
//
//    "redirect to VehicleLookupFailure after a submit and document reference number mismatch returned by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
//    }
//
//    "redirect to VehicleLookupFailure after a submit and vss error returned by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(vehicleDetailsServerDown).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
//    }

    "replace max length error message for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1))
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "")
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of the page and once above the field.
    }

    "redirect to Before You Start page when back button is pressed" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
    }

//    "redirect to MicroserviceError when microservice throws" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupError.submit(request)
//
//      whenReady(result, timeout) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
//      }
//    }
//
//    "redirect to MicroServiceError after a submit if response status is Ok and no response payload" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(vehicleDetailsNoResponse).submit(request)
//
//      // TODO This test passes for the wrong reason, it is throwing when VehicleLookupServiceImpl tries to access resp.json, whereas we want VehicleLookupServiceImpl to return None as a response payload.
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
//    }
//
//    "write cookie when vss error returned by the microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(vehicleDetailsServerDown).submit(request)
//
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain(VehicleLookupFormModelCacheKey)
//      }
//    }
//
//    "write cookie when document reference number mismatch returned by microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(fullResponse = vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain allOf(
//          BruteForcePreventionViewModelCacheKey, VehicleLookupResponseCodeCacheKey, VehicleLookupFormModelCacheKey)
//      }
//    }
//
//    "write cookie when vrm not found by the fake microservice" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupResponseGenerator(fullResponse = vehicleDetailsResponseVRMNotFound).submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain allOf(
//          BruteForcePreventionViewModelCacheKey, VehicleLookupResponseCodeCacheKey, VehicleLookupFormModelCacheKey)
//      }
//    }
//
//    "does not write cookie when microservice throws" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = vehicleLookupError.submit(request)
//
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies shouldBe empty
//      }
//    }
//
//    "redirect to vrm locked when valid submit and brute force prevention returns not permitted" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmLocked)
//      val result = vehicleLookupResponseGenerator(
//        vehicleDetailsResponseDocRefNumberNotLatest,
//        bruteForceService = bruteForceServiceImpl(permitted = false)
//      ).submit(request)
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
//    }
//
//    "redirect to VehicleLookupFailure and display 1st attempt message when document reference number not found and security service returns 1st attempt" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberValid)
//      val result = vehicleLookupResponseGenerator(
//        vehicleDetailsResponseDocRefNumberNotLatest,
//        bruteForceService = bruteForceServiceImpl(permitted = true)
//      ).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
//    }
//
//    "redirect to VehicleLookupFailure and display 2nd attempt message when document reference number not found and security service returns 2nd attempt" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmAttempt2)
//      val result = vehicleLookupResponseGenerator(
//        vehicleDetailsResponseDocRefNumberNotLatest,
//        bruteForceService = bruteForceServiceImpl(permitted = true)
//      ).submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
//    }
//
//    "Send a request and a trackingId" in new WithApplication {
//      val trackingId = "x" * 20
//      val request = buildCorrectlyPopulatedRequest().
//        withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))
//      val mockVehiclesLookupService = mock[VehicleLookupWebService]
//      when(mockVehiclesLookupService.callVehicleLookupService(any[VehicleDetailsRequest], any[String])).
//        thenReturn(Future {
//          new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
//        })
//      val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(mockVehiclesLookupService)
//      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
//      implicit val config: Config = mock[Config]
//
//      val vehiclesLookup = new vrm_retention.VehicleLookup(
//        bruteForceServiceImpl(permitted = true),
//        vehicleLookupServiceImpl
//      )
//      val result = vehiclesLookup.submit(request)
//
//      whenReady(result) { r =>
//        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
//        verify(mockVehiclesLookupService).callVehicleLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
//        trackingIdCaptor.getValue should be(trackingId)
//      }
//    }
//
//    "Send the request and no trackingId if session is not present" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val mockVehiclesLookupService = mock[VehicleLookupWebService]
//      when(mockVehiclesLookupService.callVehicleLookupService(any[VehicleDetailsRequest], any[String])).thenReturn(Future {
//        new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
//      })
//      val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(mockVehiclesLookupService)
//      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
//      implicit val config: Config = mock[Config]
//      val vehiclesLookup = new vrm_retention.VehicleLookup(
//        bruteForceServiceImpl(permitted = true),
//        vehicleLookupServiceImpl)
//      val result = vehiclesLookup.submit(request)
//
//      whenReady(result) { r =>
//        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
//        verify(mockVehiclesLookupService).callVehicleLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
//        trackingIdCaptor.getValue should be(ClearTextClientSideSessionFactory.DefaultTrackingId)
//      }
//    }

  }

  private final val ExitAnchorHtml = """a id="exit""""

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
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    new vrm_retention.VehicleLookup(
      bruteForceService = bruteForceService,
      vehicleLookupService = vehicleLookupServiceImpl)
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
    new vrm_retention.VehicleLookup(
      bruteForceService = bruteForceServiceImpl(permitted = permitted),
      vehicleLookupService = vehicleLookupServiceImpl)
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber)
  }

  private lazy val present = {
    val request = FakeRequest()
    vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).present(request)
  }

  private def lookupWithMockConfig(config: Config): VehicleLookup =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[VehicleLookup])

  private val testDuration = 7.days.toMillis
  private implicit val dateService = new DateServiceImpl
}