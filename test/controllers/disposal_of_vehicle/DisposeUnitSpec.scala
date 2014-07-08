package controllers.disposal_of_vehicle

import mappings.common.AddressLines
import AddressLines.BuildingNameOrNumberHolder
import AddressLines.LineMaxLength
import common.ClientSideSessionFactory
import Common.PrototypeHtml
import controllers.disposal_of_vehicle
import services.fakes.FakeDisposeWebServiceImpl
import services.fakes.FakeAddressLookupService
import FakeAddressLookupService.BuildingNameOrNumberValid
import FakeAddressLookupService.Line2Valid
import FakeAddressLookupService.Line3Valid
import FakeAddressLookupService.PostcodeValid
import FakeAddressLookupService.PostcodeValidWithSpace
import FakeAddressLookupService.PostTownValid
import FakeAddressLookupService.TraderBusinessNameValid
import FakeDisposeWebServiceImpl.disposeResponseApplicationBeingProcessed
import FakeDisposeWebServiceImpl.disposeResponseFailureWithDuplicateDisposal
import FakeDisposeWebServiceImpl.disposeResponseSuccess
import FakeDisposeWebServiceImpl.disposeResponseUnableToProcessApplication
import FakeDisposeWebServiceImpl.disposeResponseUndefinedError
import FakeDisposeWebServiceImpl.MileageValid
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs
import helpers.disposal_of_vehicle.CookieFactoryForUnitSpecs.TrackingIdValue
import helpers.UnitSpec
import helpers.WithApplication
import mappings.disposal_of_vehicle.Dispose.ConsentId
import mappings.disposal_of_vehicle.Dispose.DateOfDisposalId
import mappings.disposal_of_vehicle.Dispose.DisposeFormModelCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormRegistrationNumberCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormTimestampIdCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormTransactionIdCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeModelCacheKey
import mappings.disposal_of_vehicle.Dispose.LossOfRegistrationConsentId
import mappings.disposal_of_vehicle.Dispose.MileageId
import models.DayMonthYear
import models.domain.disposal_of_vehicle.{DisposeResponse, DisposeRequest, DisposalAddressDto}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{when, verify, times}
import pages.disposal_of_vehicle.DisposeFailurePage
import pages.disposal_of_vehicle.DisposeSuccessPage
import pages.disposal_of_vehicle.DuplicateDisposalErrorPage
import pages.disposal_of_vehicle.MicroServiceErrorPage
import pages.disposal_of_vehicle.SetupTradeDetailsPage
import pages.disposal_of_vehicle.SoapEndpointErrorPage
import pages.disposal_of_vehicle.VehicleLookupPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.SERVICE_UNAVAILABLE
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.DateService
import services.dispose_service.{DisposeServiceImpl, DisposeWebService, DisposeService}
import services.fakes.FakeDateServiceImpl.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import services.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import services.fakes.{FakeDisposeWebServiceImpl, FakeResponse}
import utils.helpers.Config

final class DisposeUnitSpec extends UnitSpec {
  "present" should {
    "display the page" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setupTradeDetails page when present and previous pages have not been visited" in new WithApplication {
      val request = FakeRequest()
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeFormModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      val content = contentAsString(result)
      val contentWithCarriageReturnsAndSpacesRemoved = content.replaceAll("[\n\r]", "").replaceAll(emptySpace, "")
      contentWithCarriageReturnsAndSpacesRemoved should include(buildCheckboxHtml("consent", checked = true))
      contentWithCarriageReturnsAndSpacesRemoved should include(
        buildCheckboxHtml("lossOfRegistrationConsent", checked = true))

      contentWithCarriageReturnsAndSpacesRemoved should include(buildSelectedOptionHtml("25", "25"))
      contentWithCarriageReturnsAndSpacesRemoved should include(buildSelectedOptionHtml("11", "November"))
      contentWithCarriageReturnsAndSpacesRemoved should include(buildSelectedOptionHtml("1970", "1970"))
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      val content = contentAsString(result)
      val contentWithCarriageReturnsAndSpacesRemoved = content.replaceAll("[\n\r]", "").replaceAll(emptySpace, "")
      contentWithCarriageReturnsAndSpacesRemoved should include(buildCheckboxHtml("consent", checked = false))
      contentWithCarriageReturnsAndSpacesRemoved should include(
        buildCheckboxHtml("lossOfRegistrationConsent", checked = false))
      content should not include "selected" // No drop downs should be selected
    }

    "display prototype message when config set to true" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).present(request)
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      implicit val config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val webService: DisposeWebService = disposeWebService()
      val disposeService = new DisposeServiceImpl(config, webService)
      val result = disposeController(disposeWebService = webService, disposeService = disposeService).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to dispose success when a success message is returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val result = disposeController(disposeWebService = disposeWebService()).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
      }
    }

    "redirect to micro-service error page when an unexpected error occurs" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceStatus = INTERNAL_SERVER_ERROR, disposeServiceResponse = None))
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to duplicate-disposal error page when an duplicate disposal error occurs" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(
        disposeWebService = disposeWebService(
          disposeServiceStatus = OK,
          disposeServiceResponse = Some(disposeResponseFailureWithDuplicateDisposal)
        )
      )
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DuplicateDisposalErrorPage.address))
      }
    }

    "redirect to setupTradeDetails page after the dispose button is clicked and no vehicleLookupFormModel is cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "return a bad request when no details are entered" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to setupTradeDetails page when form submitted with errors and previous pages have not been visited" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to micro-service error page when calling webservice throws exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeResponseThrows = mock[(Int, Option[DisposeResponse])]
      val mockWebServiceThrows = mock[DisposeService]
      when(mockWebServiceThrows.invoke(any[DisposeRequest], any[String])).thenReturn(Future.failed(new RuntimeException))
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      val dispose = new disposal_of_vehicle.Dispose(mockWebServiceThrows, dateServiceStubbed())
      val result = dispose.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to soap endpoint error page when service is unavailable" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceStatus = SERVICE_UNAVAILABLE, disposeServiceResponse = None))
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SoapEndpointErrorPage.address))
      }
    }

    "redirect to dispose success when applicationBeingProcessed" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeSuccess = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseApplicationBeingProcessed)))
      val result = disposeSuccess.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeSuccessPage.address))
      }
    }

    "redirect to dispose failure page when unableToProcessApplication" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseUnableToProcessApplication)))
      val result = disposeFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(DisposeFailurePage.address))
      }
    }

    "redirect to error page when undefined error is returned" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.disposeModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeFailure = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseUndefinedError)))
      val result = disposeFailure.submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "write cookies when a success message is returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = disposeController(disposeWebService = disposeWebService()).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        val found = cookies.find(_.name == DisposeFormTimestampIdCacheKey)
        found match {
          case Some(cookie) => cookie.value should include(CookieFactoryForUnitSpecs.disposeFormTimestamp().value)
          case _ => fail("Should have found cookie")
        }
        val expectedCacheKeys = Seq(DisposeFormModelCacheKey, DisposeFormTimestampIdCacheKey)
        cookies.map(_.name) should contain allOf(
          DisposeModelCacheKey, DisposeFormTransactionIdCacheKey, expectedCacheKeys: _*)
      }
    }

    "write cookies when applicationBeingProcessed" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val disposeSuccess = disposeController(disposeWebService =
        disposeWebService(disposeServiceResponse = Some(disposeResponseApplicationBeingProcessed)))
      val result = disposeSuccess.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        val expectedCookies = Seq(
          DisposeFormRegistrationNumberCacheKey,
          DisposeFormModelCacheKey,
          DisposeFormTimestampIdCacheKey
        )
        cookies.map(_.name) should contain allOf(
          DisposeModelCacheKey, DisposeFormTransactionIdCacheKey, expectedCookies: _*)
      }
    }

    "calls DisposeService invoke with the expected DisposeRequest" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest()

      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "send a request and a trackingId" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())
      val mockDisposeService = mock[DisposeService]
      when(mockDisposeService.invoke(any(classOf[DisposeRequest]), any[String])).
        thenReturn(Future[(Int, Option[DisposeResponse])] {
        (200, None)
      })
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      val dispose = new disposal_of_vehicle.Dispose(mockDisposeService, dateServiceStubbed())
      val result = dispose.submit(request)
      whenReady(result) { r =>
        val trackingIdCaptor = ArgumentCaptor.forClass(classOf[String])
        verify(mockDisposeService).invoke(any[DisposeRequest], trackingIdCaptor.capture())
        trackingIdCaptor.getValue should be(TrackingIdValue)
      }
    }

    "truncate address lines 1,2,3 and 4 up to max characters" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(
          buildingNameOrNumber = "a" * (LineMaxLength + 1),
          line2 = "b" * (LineMaxLength + 1),
          line3 = "c" * (LineMaxLength + 1),
          postTown = "d" * (LineMaxLength + 1)
        )).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, linePart2Truncated, linePart3Truncated),
        postTown = postTownTruncated
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate post town up to max characters" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(postTown = postTownTooLong.get)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid),
        postTown = postTownTruncated
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "remove spaces from postcode on submit" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(traderPostcode = PostcodeValidWithSpace)). // postcode contains space
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid)
      )

      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number and place remainder on line 2 when line 2 is empty" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(buildingNameOrNumber = linePart1TooLong, line2 = "")).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", Line3Valid)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }


    "truncate address line 2 and place remainder on line 3 when line 3 is empty" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(line2 = linePart2TooLong, line3 = "")).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, linePart2Truncated, "b")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number and place remainder on line 2 when line 3 is empty. Line 2 is over max length, should be placed on line 3 and truncated" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(
          buildingNameOrNumber = linePart1TooLong,
          line2 = linePart2TooLong,
          line3 = ""
        )).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", linePart2Truncated)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number when over 30 characters. Move line 2 to line 3 and remainder of building name or number to line 2 when line 3 is empty" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(buildingNameOrNumber = linePart1TooLong, line3 = "")).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", Line2Valid)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number, create line 2 and move reaminder to line2  when only building name or number, town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelBuildingNameOrNumber(
          buildingNameOrNumber = linePart1TooLong)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate address line 2, create line 3 and move reaminder to line3 when only building name or number, line2, town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelLine2(line2 = linePart2TooLong)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberValid, linePart2Truncated, "b")
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "truncate building name or number, create line 3, move line 2 to line 3 remainder of building name or number to line 2 when building name or number, line2, town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelLine2(
          buildingNameOrNumber = linePart1TooLong, line2 = linePart2TooLong)).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(linePart1Truncated, "a", linePart2Truncated)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "create dummy building name or number when only town and postcode returned" in new WithApplication {
      val disposeService = disposeServiceMock()
      val controller = disposeController(disposeWebService = disposeWebService(), disposeService = disposeService)

      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModelPostTown()).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel())

      val result = controller.submit(request)

      val disposeRequest = expectedDisposeRequest(
        line = Seq(BuildingNameOrNumberHolder)
      )
      verify(disposeService, times(1)).invoke(cmd = disposeRequest, trackingId = TrackingIdValue)
    }

    "redirect to vehicle lookup when a dispose success cookie exists" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest.
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.preventGoingToDisposePage())

      val result = disposeController(disposeWebService = disposeWebService()).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  private val dateValid: String = DayMonthYear(
    DateOfDisposalDayValid.toInt,
    DateOfDisposalMonthValid.toInt,
    DateOfDisposalYearValid.toInt
  ).toDateTime.get.toString

  private val emptySpace = " "

  private def dateServiceStubbed(day: Int = DateOfDisposalDayValid.toInt,
                                 month: Int = DateOfDisposalMonthValid.toInt,
                                 year: Int = DateOfDisposalYearValid.toInt) = {
    val dateService = mock[DateService]
    when(dateService.today).thenReturn(new models.DayMonthYear(day = day,
      month = month,
      year = year))
    dateService
  }

  private val buildCorrectlyPopulatedRequest = {
    import mappings.common.DayMonthYear._
    FakeRequest().withFormUrlEncodedBody(
      MileageId -> MileageValid,
      s"$DateOfDisposalId.$DayId" -> DateOfDisposalDayValid,
      s"$DateOfDisposalId.$MonthId" -> DateOfDisposalMonthValid,
      s"$DateOfDisposalId.$YearId" -> DateOfDisposalYearValid,
      ConsentId -> FakeDisposeWebServiceImpl.ConsentValid,
      LossOfRegistrationConsentId -> FakeDisposeWebServiceImpl.ConsentValid
    )
  }

  private def disposeServiceMock(): DisposeService = {
    val disposeServiceMock = mock[DisposeService]
    when(disposeServiceMock.invoke(any[DisposeRequest], any[String])).thenReturn(Future {
      (0, None)
    })
    disposeServiceMock
  }

  private def disposeWebService(disposeServiceStatus: Int = OK,
                                disposeServiceResponse: Option[DisposeResponse] = Some(disposeResponseSuccess)): DisposeWebService = {
    val disposeWebService = mock[DisposeWebService]
    when(disposeWebService.callDisposeService(any[DisposeRequest], any[String])).thenReturn(Future {
      val fakeJson = disposeServiceResponse map (Json.toJson(_))
      new FakeResponse(status = disposeServiceStatus, fakeJson = fakeJson) // Any call to a webservice will always return this successful response.
    })

    disposeWebService
  }

  private val config: Config = {
    val config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(true)
    config
  }

  private def disposeController(disposeWebService: DisposeWebService): Dispose = {
    val disposeService = new DisposeServiceImpl(config, disposeWebService)
    disposeController(disposeWebService, disposeService)
  }

  private def disposeController(disposeWebService: DisposeWebService, disposeService: DisposeService)(implicit config: Config = config): Dispose = {
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])

    new disposal_of_vehicle.Dispose(disposeService, dateServiceStubbed())
  }


  private def buildCheckboxHtml(widgetName: String, checked: Boolean): String = {
    if (checked)
      s"""<inputtype="checkbox"id="$widgetName"name="$widgetName"value="true"checkedaria-required=true>"""
    else
      s"""<inputtype="checkbox"id="$widgetName"name="$widgetName"value="true"aria-required=true>"""
  }

  private def buildSelectedOptionHtml(optionValue: String, optionText: String): String = {
    s"""<optionvalue="$optionValue"selected>$optionText</option>"""
  }

  private val linePart1Truncated: String = "a" * LineMaxLength
  private val linePart2Truncated: String = "b" * LineMaxLength
  private val linePart3Truncated: String = "c" * LineMaxLength
  private val linePart1TooLong: String = linePart1Truncated + "a"
  private val linePart2TooLong: String = linePart2Truncated + "b"
  private val postTownTruncated: Option[String] = Some("d" * LineMaxLength)
  private val postTownTooLong: Option[String] = Some("d" * (LineMaxLength + 1))

  private def expectedDisposeRequest(referenceNumber: String = ReferenceNumberValid,
                                     registrationNumber: String = RegistrationNumberValid,
                                     traderName: String = TraderBusinessNameValid,
                                     line: Seq[String] = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid),
                                     postTown: Option[String] = Some(PostTownValid),
                                     postCode: String = PostcodeValid,
                                     uprn: Option[Long] = None,
                                     dateOfDisposal: String = dateValid,
                                     transactionTimestamp: String = dateValid,
                                     prConsent: Boolean = FakeDisposeWebServiceImpl.ConsentValid.toBoolean,
                                     keeperConsent: Boolean = FakeDisposeWebServiceImpl.ConsentValid.toBoolean,
                                     mileage: Option[Int] = Some(MileageValid.toInt)) =
    DisposeRequest(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      traderName = traderName,
      traderAddress = DisposalAddressDto(
        line = line,
        postTown = postTown,
        postCode = postCode,
        uprn = uprn
      ),
      dateOfDisposal = dateOfDisposal,
      transactionTimestamp = transactionTimestamp,
      prConsent = prConsent,
      keeperConsent = keeperConsent,
      mileage = mileage
    )
}