package controllers.disposal_of_vehicle

import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers._
import controllers.disposal_of_vehicle
import mappings.disposal_of_vehicle.VehicleLookup._
import helpers.disposal_of_vehicle.Helper._
import org.mockito.Mockito._
import org.mockito.Matchers._
import models.domain.disposal_of_vehicle.{VehicleDetailsResponse, VehicleDetailsRequest}
import services.fakes.FakeResponse
import pages.disposal_of_vehicle._
import helpers.disposal_of_vehicle.{CookieFactory, CacheSetup}
import helpers.UnitSpec
import services.vehicle_lookup.{VehicleLookupServiceImpl, VehicleLookupWebService}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{JsValue, Json}
import ExecutionContext.Implicits.global
import services.fakes.FakeVehicleLookupWebService._
import services.fakes.FakeAddressLookupService._
import play.api.http.Status.OK
import services.session.PlaySessionState
import services.fakes.FakeWebServiceImpl._
import play.api.mvc.Cookies

class VehicleLookupUnitSpec extends UnitSpec {

  "VehicleLookup - Controller" should {

    "present" in new WithApplication {
      val request = FakeRequest().withSession().
        withCookies(CookieFactory.dealerDetails())
      val result = vehicleLookupResponseGenerator( vehicleDetailsResponseSuccess).present(request)

      result.futureValue.header.status should equal(OK)
    }

    "redirect to Dispose after a valid submit and true message returned from the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator( vehicleDetailsResponseSuccess).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(DisposePage.address))
    }

    "submit removes spaces from registrationNumber" in new WithApplication {
      // DE7 Spaces should be stripped
      val request = buildCorrectlyPopulatedRequest(registrationNumber = registrationNumberWithSpaceValid)
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).submit(request)

      whenReady(result) {
        r =>
          val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
          val foundMatch =  cookies.exists(cookie => cookie.equals(CookieFactory.vehicleLookupFormModel(registrationNumber = registrationNumberValid)))
          foundMatch should equal(true)
      }
    }

    "redirect to VehicleLookupFailure after a submit and no response code and no vehicledetailsdto returned from the fake microservice" in new WithApplication {
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

      result.futureValue.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
    }

    "redirect to setupTradeDetails page when user has not set up a trader for disposal" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).present(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "return a bad request if dealer details are in cache and no details are entered" in new WithApplication {

      val request = buildCorrectlyPopulatedRequest(referenceNumber = "", registrationNumber = "", consent = "").
        withCookies(CookieFactory.dealerDetails())
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).submit(request)

      result.futureValue.header.status should equal(BAD_REQUEST)
    }

    "redirect to setupTradeDetails page if dealer details are not in cache and no details are entered" in new WithApplication {

      val request = buildCorrectlyPopulatedRequest(referenceNumber = "", registrationNumber = "", consent = "")
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "replace max length error message for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (referenceNumberLength + 1)).
        withCookies(CookieFactory.dealerDetails())
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).submit(request)
      // check the validation summary text
      countSubstring(contentAsString(result), "Document reference number - Document reference number must be an 11-digit number") should equal(1)
      // check the form item validation
      countSubstring(contentAsString(result), "\"error\">Document reference number must be an 11-digit number") should equal(1)
    }

    "replace required and min length error messages for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "").
        withCookies(CookieFactory.dealerDetails())
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).submit(request)
      // check the validation summary text
      countSubstring(contentAsString(result), "Document reference number - Document reference number must be an 11-digit number") should equal(1)
      // check the form item validation
      countSubstring(contentAsString(result), "\"error\">Document reference number must be an 11-digit number") should equal(1)
    }

    "replace max length error message for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX").
        withCookies(CookieFactory.dealerDetails())
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).submit(request)
      val count = countSubstring(contentAsString(result), "Must be valid format")

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "").
        withCookies(CookieFactory.dealerDetails())
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).submit(request)
      val count = countSubstring(contentAsString(result), "Must be valid format")

      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of the page and once above the field.
    }

    "redirect to EnterAddressManually when back button is pressed and there is no uprn" in new WithApplication {
      val request = FakeRequest().withSession().withFormUrlEncodedBody().
        withCookies(CookieFactory.dealerDetails())
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(EnterAddressManuallyPage.address))
    }

    "redirect to BusinessChooseYourAddress when back button is pressed and there is a uprn" in new WithApplication {
      val request = FakeRequest().withSession().withFormUrlEncodedBody().
        withCookies(CookieFactory.dealerDetails(uprn = Some(traderUprnValid)))
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to SetupTradeDetails page when back button is pressed and dealer details is not in cache" in new WithApplication {
      val request = FakeRequest().withSession().withFormUrlEncodedBody()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "redirect to SetUpTradeDetails when back button and the user has completed the vehicle lookup form" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactory.dealerDetails(uprn = Some(traderUprnValid)))
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to SetUpTradeDetails when back button clicked and there are no trader details stored in cache" in new WithApplication {
      // No cache setup with dealer details
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "redirect to MicroserviceError when microservice throws" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupError().submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }

    "redirect to MicroServiceError after a submit if response status is Ok and no response payload" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator(vehicleDetailsNoResponse).submit(request)

      // TODO This test passes for the wrong reason, it is throwing when VehicleLookupServiceImpl tries to access resp.json, whereas we want VehicleLookupServiceImpl to return None as a response payload.
      result.futureValue.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
    }
  }

  private def vehicleLookupResponseGenerator( fullResponse:(Int, Option[VehicleDetailsResponse])) = {
  val ws: VehicleLookupWebService = mock[VehicleLookupWebService]
    when(ws.callVehicleLookupService(any[VehicleDetailsRequest])).thenReturn(Future {
      val responseAsJson : Option[JsValue] = fullResponse._2 match {
        case Some(e) => Some(Json.toJson(e))
        case _ => None
      }
      new FakeResponse(status = fullResponse._1, fakeJson = responseAsJson)// Any call to a webservice will always return this successful response.
    })
    val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(ws)
    new disposal_of_vehicle.VehicleLookup( vehicleLookupServiceImpl)
  }

  private def vehicleLookupError() = {
    val ws: VehicleLookupWebService = mock[VehicleLookupWebService]
    when(ws.callVehicleLookupService(any[VehicleDetailsRequest])).thenReturn(Future {
      throw new IllegalArgumentException
    })
    val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(ws)
    new disposal_of_vehicle.VehicleLookup( vehicleLookupServiceImpl)
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = referenceNumberValid,
                                             registrationNumber: String = registrationNumberValid,
                                             consent: String = consentValid) = {
    FakeRequest().withSession().withFormUrlEncodedBody(
      referenceNumberId -> referenceNumber,
      registrationNumberId -> registrationNumber,
      consentId -> consent)
  }

}
