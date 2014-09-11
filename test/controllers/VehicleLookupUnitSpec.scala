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
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsResponse, VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupWebService}
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import utils.helpers.Config
import webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsResponse
import views.vrm_retention.VehicleLookup.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import scala.concurrent.Future

final class VehicleLookupUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = vehicleLookupStubs().present(request)
      val content = contentAsString(result)
      content should include(ReferenceNumberValid)
      content should include(RegistrationNumberValid)
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleLookupStubs().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = vehicleLookupStubs(isPrototypeBannerVisible = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  // TODO Why is this commented out?
  "submit" should {
    //    "redirect to Confirm after a valid submit and true message returned from the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs().submit(request)
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
    //      val result = vehicleLookupStubs().submit(request)
    //
    //      whenReady(result) { r =>
    //        val cookies = fetchCookiesFromHeaders(r)
    //        cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
    //      }
    //    }
    //
    //    "redirect to MicroServiceError after a submit and no response code and no vehicledetailsdto returned from the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs(vehicleDetailsResponseNotFoundResponseCode).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure after a submit and vrm not found by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs(vehicleDetailsResponseVRMNotFound).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure after a submit and document reference number mismatch returned by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs(vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure after a submit and vss error returned by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs(vehicleDetailsServerDown).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    //    }

    "replace max length error message for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1))
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "")
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of the page and once above the field.
    }

    "redirect to Before You Start page when back button is pressed" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleLookupStubs().back(request)

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
    //      val result = vehicleLookupStubs(vehicleDetailsNoResponse).submit(request)
    //
    //      // TODO This test passes for the wrong reason, it is throwing when VehicleAndKeeperLookupServiceImpl tries to access resp.json, whereas we want VehicleAndKeeperLookupServiceImpl to return None as a response payload.
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    //    }
    //
    //    "write cookie when vss error returned by the microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs(vehicleDetailsServerDown).submit(request)
    //
    //      whenReady(result) { r =>
    //        val cookies = fetchCookiesFromHeaders(r)
    //        cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
    //      }
    //    }
    //
    //    "write cookie when document reference number mismatch returned by microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs(fullResponse = vehicleDetailsResponseDocRefNumberNotLatest).submit(request)
    //      whenReady(result) { r =>
    //        val cookies = fetchCookiesFromHeaders(r)
    //        cookies.map(_.name) should contain allOf(
    //          BruteForcePreventionViewModelCacheKey, VehicleAndKeeperLookupResponseCodeCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
    //      }
    //    }
    //
    //    "write cookie when vrm not found by the fake microservice" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest()
    //      val result = vehicleLookupStubs(fullResponse = vehicleDetailsResponseVRMNotFound).submit(request)
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
    //      val result = vehicleLookupStubs(
    //        vehicleDetailsResponseDocRefNumberNotLatest,
    //        bruteForceService = bruteForceServiceImpl(permitted = false)
    //      ).submit(request)
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure and display 1st attempt message when document reference number not found and security service returns 1st attempt" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberValid)
    //      val result = vehicleLookupStubs(
    //        vehicleDetailsResponseDocRefNumberNotLatest,
    //        bruteForceService = bruteForceServiceImpl(permitted = true)
    //      ).submit(request)
    //
    //      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleAndKeeperLookupFailurePage.address))
    //    }
    //
    //    "redirect to VehicleAndKeeperLookupFailure and display 2nd attempt message when document reference number not found and security service returns 2nd attempt" in new WithApplication {
    //      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmAttempt2)
    //      val result = vehicleLookupStubs(
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
  private lazy val present = {
    val request = FakeRequest()
    vehicleLookupStubs(vehicleAndKeeperDetailsResponseSuccess).present(request)
  }

  private def responseThrows: Future[WSResponse] = Future.failed(new RuntimeException("This error is generated deliberately by a test"))

  private def vehicleLookupStubs(fullResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = vehicleAndKeeperDetailsResponseSuccess,
                                 isPrototypeBannerVisible: Boolean = true,
                                 permitted: Boolean = true) = {
    testInjectorOverrideDev(new ScalaModule() {
      override def configure(): Unit = {
        // Stub VehicleAndKeeperLookupService
        val (responseStatus, vehicleAndKeeperDetailsResponse) = fullResponse
        val ws: VehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
        when(ws.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).thenReturn(Future.successful {
          val responseAsJson: Option[JsValue] = vehicleAndKeeperDetailsResponse match {
            case Some(e) => Some(Json.toJson(e))
            case _ => None
          }
          new FakeResponse(status = responseStatus, fakeJson = responseAsJson) // Any call to a webservice will always return this successful response.
        })
        bind[VehicleAndKeeperLookupWebService].toInstance(ws)

        // Stub config
        val config: Config = mock[Config]
        when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
        bind[Config].toInstance(config)

        // Stub BruteForcePreventionWebService
        val bruteForceStatus = if (permitted) play.api.http.Status.OK else play.api.http.Status.FORBIDDEN
        val bruteForcePreventionWebService = mock[BruteForcePreventionWebService]

        when(bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid)).
          thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseFirstAttempt)))

        when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmAttempt2)).
          thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseSecondAttempt)))

        when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmLocked)).
          thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus)))

        when(bruteForcePreventionWebService.callBruteForce(VrmThrows)).
          thenReturn(responseThrows)

        bind[BruteForcePreventionWebService].toInstance(bruteForcePreventionWebService)
      }
    }).getInstance(classOf[VehicleLookup])
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber)
  }
}
