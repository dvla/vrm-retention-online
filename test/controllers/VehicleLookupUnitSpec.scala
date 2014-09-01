package controllers

import com.tzavellas.sse.guice.ScalaModule
import controllers.Common.PrototypeHtml
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.vrm_retention._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import services.fakes.BruteForcePreventionWebServiceConstants.{VrmThrows, responseFirstAttempt, responseSecondAttempt}
import services.fakes.VehicleAndKeeperLookupWebServiceConstants.{ReferenceNumberValid, RegistrationNumberValid, vehicleAndKeeperDetailsResponseSuccess}
import services.fakes.{BruteForcePreventionWebServiceConstants, FakeResponse}
import services.vehicle_and_keeper_lookup.{VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber
import uk.gov.dvla.vehicles.presentation.common.services.{DateService, DateServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionConfig, BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import utils.helpers.Config
import viewmodels.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse}
import views.vrm_retention.VehicleLookup.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

final class VehicleLookupUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = vehicleAndKeeperLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(ReferenceNumberValid)
      content should include(RegistrationNumberValid)
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleAndKeeperLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleLookupPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  // TODO Why is this commented out?
  "submit" should {
    //    "redirect to Confirm after a valid submit and true message returned from the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator().submit(request)
    //
    //      whenReady(result, timeout) { r =>
    //        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
    //        val cookies = fetchCookiesFromHeaders(r)
    //        val cookieName = "vehicleAndKeeperLookupFormModel"
    //        cookies.find(_.name == cookieName) match {
    //          case Some(cookie) =>
    //            val json = cookie.value
    //            val model = deserializeJsonToModel[VehicleAndKeeperLookupFormModel](json)
    //            model.registrationNumber should equal(RegistrationNumberValid.toUpperCase)
    //          case None => fail(s"$cookieName cookie not found")
    //        }
    //      }
    //    }
    //
    //    "submit removes spaces from registrationNumber" in new WithApplication {
    //      // DE7 Spaces should be stripped
    //      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
    //      val result = vehicleAndKeeperLookupResponseGenerator().submit(request)
    //
    //      whenReady(result) { r =>
    //        val cookies = fetchCookiesFromHeaders(r)
    //        cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
    //      }
    //    }
    //
    //    "redirect to MicroServiceError after a submit and no response code and no vehicledetailsdto returned from the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(vehicleDetailsResponseNotFoundResponseCode).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure after a submit and vrm not found by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(vehicleDetailsResponseVRMNotFound).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure after a submit and document reference number mismatch returned by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure after a submit and vss error returned by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(vehicleDetailsServerDown).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    //    }

    "replace max length error message for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1))
      val result = vehicleAndKeeperLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "")
      val result = vehicleAndKeeperLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
      val result = vehicleAndKeeperLookupResponseGenerator().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
      val result = vehicleAndKeeperLookupResponseGenerator().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of the page and once above the field.
    }

    "redirect to Before You Start page when back button is pressed" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleAndKeeperLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
    }

    //    "redirect to MicroserviceError when microservice throws" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupError.submit(request)
    //
    //      whenReady(result, timeout) { r =>
    //        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    //      }
    //    }
    //
    //    "redirect to MicroServiceError after a submit if response status is Ok and no response payload" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(vehicleDetailsNoResponse).submit(request)
    //
    //      // TODO This test passes for the wrong reason, it is throwing when VehicleAndKeeperLookupServiceImpl tries to access resp.json, whereas we want VehicleAndKeeperLookupServiceImpl to return None as a response payload.
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    //    }
    //
    //    "write cookie when vss error returned by the microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(vehicleDetailsServerDown).submit(request)
    //
    //      whenReady(result) { r =>
    //        val cookies = fetchCookiesFromHeaders(r)
    //        cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
    //      }
    //    }
    //
    //    "write cookie when document reference number mismatch returned by microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(fullResponse = vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
    //      whenReady(result) { r =>
    //        val cookies = fetchCookiesFromHeaders(r)
    //        cookies.map(_.name) should contain allOf(
    //          BruteForcePreventionViewModelCacheKey, VehicleAndKeeperLookupResponseCodeCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
    //      }
    //    }
    //
    //    "write cookie when vrm not found by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupResponseGenerator(fullResponse = vehicleDetailsResponseVRMNotFound).submit(request)
    //      whenReady(result) { r =>
    //        val cookies = fetchCookiesFromHeaders(r)
    //        cookies.map(_.name) should contain allOf(
    //          BruteForcePreventionViewModelCacheKey, VehicleAndKeeperLookupResponseCodeCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
    //      }
    //    }
    //
    //    "does not write cookie when microservice throws" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleAndKeeperLookupError.submit(request)
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
    //      val result = vehicleAndKeeperLookupResponseGenerator(
    //        vehicleDetailsResponseDocRefNumberNotLatest,
    //        bruteForceService = bruteForceServiceImpl(permitted = false)
    //      ).submit(request)
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure and display 1st attempt message when document reference number not found and security service returns 1st attempt" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberValid)
    //      val result = vehicleAndKeeperLookupResponseGenerator(
    //        vehicleDetailsResponseDocRefNumberNotLatest,
    //        bruteForceService = bruteForceServiceImpl(permitted = true)
    //      ).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure and display 2nd attempt message when document reference number not found and security service returns 2nd attempt" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmAttempt2)
    //      val result = vehicleAndKeeperLookupResponseGenerator(
    //        vehicleDetailsResponseDocRefNumberNotLatest,
    //        bruteForceService = bruteForceServiceImpl(permitted = true)
    //      ).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
    //    }
    //
    //    "Send a request and a trackingId" in new WithApplication {
    //      val trackingId = "x" * 20
    //      val request = buildCorrectlyPopulatedRequest().
    //        withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))
    //      val mockVehiclesLookupService = mock[VehicleAndKeeperLookupWebService]
    //      when(mockVehiclesLookupService.callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], any[String])).
    //        thenReturn(Future {
    //          new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
    //        })
    //      val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(mockVehiclesLookupService)
    //      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    //      implicit val config: Config = mock[Config]
    //
    //      val vehiclesLookup = new vrm_retention.VehicleAndKeeperLookup(
    //        bruteForceServiceImpl(permitted = true),
    //        vehicleAndKeeperLookupServiceImpl
    //      )
    //      val result = vehiclesLookup.submit(request)
    //
    //      whenReady(result) { r =>
    //        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
    //        verify(mockVehiclesLookupService).callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
    //        trackingIdCaptor.getValue should be(trackingId)
    //      }
    //    }
    //
    //    "Send the request and no trackingId if session is not present" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val mockVehiclesLookupService = mock[VehicleAndKeeperLookupWebService]
    //      when(mockVehiclesLookupService.callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], any[String])).thenReturn(Future {
    //        new FakeResponse(status = 200, fakeJson = Some(Json.toJson(vehicleDetailsResponseSuccess._2.get)))
    //      })
    //      val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(mockVehiclesLookupService)
    //      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    //      implicit val config: Config = mock[Config]
    //      val vehiclesLookup = new vrm_retention.VehicleAndKeeperLookup(
    //        bruteForceServiceImpl(permitted = true),
    //        vehicleAndKeeperLookupServiceImpl)
    //      val result = vehiclesLookup.submit(request)
    //
    //      whenReady(result) { r =>
    //        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
    //        verify(mockVehiclesLookupService).callVehicleAndKeeperLookupService(any[VehicleDetailsRequest], trackingIdCaptor.capture())
    //        trackingIdCaptor.getValue should be(ClearTextClientSideSessionFactory.DefaultTrackingId)
    //      }
    //    }
  }

  private final val ExitAnchorHtml = """a id="exit""""
  private lazy val vehicleAndKeeperLookupError = {
    val permitted = true // The lookup is permitted as we want to test failure on the vehicle lookup micro-service step.
    val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]

    when(vehicleAndKeeperLookupWebService.callVehicleAndKeeperLookupService(any[VehicleAndKeeperDetailsRequest], any[String])).
      thenReturn(Future.failed(new IllegalArgumentException))

    val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(vehicleAndKeeperLookupWebService)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    new VehicleLookup(
      bruteForceService = bruteForceServiceImpl(permitted = permitted),
      vehicleAndKeeperLookupService = vehicleAndKeeperLookupServiceImpl,
      dateService = dateService)
  }
  private lazy val present = {
    val request = FakeRequest()
    vehicleAndKeeperLookupResponseGenerator(vehicleAndKeeperDetailsResponseSuccess).present(request)
  }
  private val testDuration = 7.days.toMillis
  private implicit val dateService = new DateServiceImpl

  private def bruteForceServiceImpl(permitted: Boolean): BruteForcePreventionService = {
    def bruteForcePreventionWebService: BruteForcePreventionWebService = {
      val status = if (permitted) play.api.http.Status.OK else play.api.http.Status.FORBIDDEN
      val bruteForcePreventionWebService = mock[BruteForcePreventionWebService]

      when(bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid)).
        thenReturn(Future.successful(new FakeResponse(status = status, fakeJson = responseFirstAttempt)))

      when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmAttempt2)).
        thenReturn(Future.successful(new FakeResponse(status = status, fakeJson = responseSecondAttempt)))

      when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmLocked)).
        thenReturn(Future.successful(new FakeResponse(status = status)))

      when(bruteForcePreventionWebService.callBruteForce(VrmThrows)).
        thenReturn(responseThrows)

      bruteForcePreventionWebService
    }

    new BruteForcePreventionServiceImpl(
      config = new BruteForcePreventionConfig(),
      ws = bruteForcePreventionWebService,
      dateService = injector.getInstance(classOf[DateService])
    )
  }

  private def responseThrows: Future[WSResponse] = Future.failed(new RuntimeException("This error is generated deliberately by a test"))

  private def vehicleAndKeeperLookupResponseGenerator(fullResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = vehicleAndKeeperDetailsResponseSuccess,
                                                      bruteForceService: BruteForcePreventionService = bruteForceServiceImpl(permitted = true),
                                                      isPrototypeBannerVisible: Boolean = true) = {
    val (status, vehicleAndKeeperDetailsResponse) = fullResponse
    val ws: VehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
    when(ws.callVehicleAndKeeperLookupService(any[VehicleAndKeeperDetailsRequest], any[String])).thenReturn(Future.successful {
      val responseAsJson: Option[JsValue] = vehicleAndKeeperDetailsResponse match {
        case Some(e) => Some(Json.toJson(e))
        case _ => None
      }
      new FakeResponse(status = status, fakeJson = responseAsJson) // Any call to a webservice will always return this successful response.
    })
    val vehicleAndKeeperLookupServiceImpl = new VehicleAndKeeperLookupServiceImpl(ws)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    new VehicleLookup(
      bruteForceService = bruteForceService,
      vehicleAndKeeperLookupService = vehicleAndKeeperLookupServiceImpl,
      dateService = dateService)
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber)
  }

  private def lookupWithMockConfig(config: Config): VehicleLookup =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[VehicleLookup])

  private def vehicleLookupPrototypeNotVisible = {
    testInjector(new ScalaModule() {
      override def configure(): Unit = {
        val config: Config = mock[Config]
        when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
        bind[Config].toInstance(config)
      }
    }).getInstance(classOf[VehicleLookup])
  }
}
