package controllers

import _root_.webserviceclients.audit2.AuditService
import _root_.webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.VrmLocked
import composition.TestConfig
import composition.webserviceclients.bruteforceprevention.TestBruteForcePreventionWebService
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallDocRefNumberNotLatest
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallServerDown
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallVRMNotFound
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupCallFails
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupCallNoResponse
import composition.webserviceclients.vrmretentioneligibility.EligibilityWebServiceCallWithResponse
import helpers.TestWithApplication
import controllers.Common.PrototypeHtml
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import models.CacheKeyPrefix
import models.IdentifierCacheKey
import models.VehicleAndKeeperLookupFormModel
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.CheckEligibilityPage
import pages.vrm_retention.MicroServiceErrorPage
import pages.vrm_retention.VehicleLookupFailurePage
import pages.vrm_retention.VrmLockedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, TrackingId}
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel.MsResponseCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.DmsWebHeaderDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import vehicleandkeeperlookup.VehicleAndKeeperLookupSuccessResponse
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.VehicleLookup.DocumentReferenceNumberId
import views.vrm_retention.VehicleLookup.KeeperConsentId
import views.vrm_retention.VehicleLookup.PostcodeId
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_retention.VehicleLookup.VehicleRegistrationNumberId
import org.mockito.Mockito.{times, verify}


class VehicleLookupUnitSpec extends UnitSpec {

  final val PostcodeInvalid = "XX99XX"
  final val TransactionIdValid = "AB12AWR701125000000" // <vrm><timestamp> format in VehicleLookup.transactionId
  // vrm part derived from buildCorrectlyPopulatedRequest (default RegistrationNumberValid)
  // timestamp part derived from vehicleLookupAndAuditStubs (DateService instantiated via Guice binding in TestComposition->TestDateService)


  "present" should {
    "display the page" in new TestWithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "not contain an identifier cookie if default route" in new TestWithApplication {
      whenReady(present) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(_.name == IdentifierCacheKey) should equal(None)
      }
    }

    "contain an identifier cookie of type ceg if ceg route" in new TestWithApplication {
      val result = vehicleLookupStubs().ceg(FakeRequest())
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(_.name == IdentifierCacheKey).get.value should equal(vehicleLookupStubs().identifier)
      }
    }

    "display empty fields when cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
      val result = vehicleLookupStubs().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      val result = vehicleLookupStubs(isPrototypeBannerVisible = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to CheckEligibility controller after a valid submit and " +
      "true message returned from the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService)
      val result = vehicleLookupStubs().submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(CheckEligibilityPage.address))
          val cookies = fetchCookiesFromHeaders(r)

          val cookieName = VehicleAndKeeperLookupFormModelCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[VehicleAndKeeperLookupFormModel](json)
              model.registrationNumber should equal(RegistrationNumberValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }

          val cookie2Name = vehicleAndKeeperLookupDetailsCacheKey
          cookies.find(_.name == cookie2Name) match {
            case Some(cookie) =>
              val json = cookie.value
              deserializeJsonToModel[VehicleAndKeeperDetailsModel](json)
            case None => fail(s"$cookie2Name cookie not found")
          }
      }
    }

    "submit removes spaces from registrationNumber" in new TestWithApplication {
      // DE7 Spaces should be stripped
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
      val result = vehicleLookupStubs().submit(request)

      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "redirect to MicroServiceError after a submit and no response code and " +
      "no vehicledetailsdto returned from the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseNotFoundResponseCode).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vrm not found by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseVRMNotFound).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "document reference number mismatch returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseDocRefNumberNotLatest).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseExportedFailure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseExportedFailure).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseScrappedFailure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseScrappedFailure).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseDamagedFailure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDamagedFailure
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseVICFailure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseVICFailure
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseNoKeeperFailure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseNoKeeperFailure
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseNotMotFailure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseNotMotFailure
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponsePre1998Failure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponsePre1998Failure
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseQFailure returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseQFailure
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vss error returned by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsServerDown
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "replace max length error message for document reference number with " +
      "standard error message (US43)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1))
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with " +
      "standard error message (US43)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "")
      val result = vehicleLookupStubs().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration mark with " +
      "standard error message (US43)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with " +
      "standard error message (US43)" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length
      // The same message is displayed in 2 places -
      // once in the validation-summary at the top of the page and once above the field.
      count should equal(2)
    }

    "redirect to MicroserviceError page when vehicleAndKeeperLookup throws an exception" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails.submit(request)

      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "does not write VehicleAndKeeperDetailsModel cookie when microservice throws an exception" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails.submit(request)

      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should not contain vehicleAndKeeperLookupDetailsCacheKey
      }
    }

    "redirect to MicroServiceError after a submit if response status is Ok and " +
      "no response payload" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallNoResponse.submit(request)

      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "write cookie when vss error returned by the microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallServerDown.submit(request)

      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "write cookie when vrm not found by the fake microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallVRMNotFound.submit(request)
      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain allOf(
            PaymentTransNoCacheKey, TransactionIdCacheKey, bruteForcePreventionViewModelCacheKey,
            MsResponseCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "redirect to vrm locked when valid submit and brute force prevention returns not permitted" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmLocked)
      val result = vehicleLookupStubs(permitted = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure and " +
      "display 1st attempt message when document reference number not found and " +
      "security service returns 1st attempt" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest.submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "write cookie when document reference number mismatch returned by microservice" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest.submit(request)
      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain allOf(
            bruteForcePreventionViewModelCacheKey,
            MsResponseCacheKey,
            VehicleAndKeeperLookupFormModelCacheKey
          )
      }
    }

    "redirect to VehicleAndKeeperLookupFailure and " +
      "display 2nd attempt message when document reference number not found and " +
      "security service returns 2nd attempt" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(
        registrationNumber = BruteForcePreventionWebServiceConstants.VrmAttempt2
      )
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
      ).submit(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "send a request and a trackingId to the vehicleAndKeeperLookupWebService" in new TestWithApplication {
      val trackingId = TrackingId("default_test_tracking_id")
      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))
      val (vehicleLookup, dateService, vehicleAndKeeperLookupWebService) = vehicleLookupStubs
      val result = vehicleLookup.submit(request)

      whenReady(result) {
        r =>
          val expectedRequest = VehicleAndKeeperLookupRequest(
            dmsHeader = buildHeader(trackingId, dateService),
            referenceNumber = ReferenceNumberValid,
            registrationNumber = RegistrationNumberValid,
            transactionTimestamp = dateService.now.toDateTime
          )
          verify(vehicleAndKeeperLookupWebService).invoke(request = expectedRequest, trackingId = trackingId)
      }
    }

    "send a request and default trackingId to the vehicleAndKeeperLookupWebService when " +
      "cookie does not exist" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService)
      val (vehicleLookup, dateService, vehicleAndKeeperLookupWebService) = vehicleLookupStubs
      val result = vehicleLookup.submit(request)

      whenReady(result) {
        r =>
          val expectedRequest = VehicleAndKeeperLookupRequest(
            dmsHeader = buildHeader(ClearTextClientSideSessionFactory.DefaultTrackingId, dateService),
            referenceNumber = ReferenceNumberValid,
            registrationNumber = RegistrationNumberValid,
            transactionTimestamp = dateService.now.toDateTime
          )
          verify(vehicleAndKeeperLookupWebService).invoke(
            request = expectedRequest,
            trackingId = ClearTextClientSideSessionFactory.DefaultTrackingId
          )
      }
    }

    "call audit service with 'default_test_tracking_id' when DocRefNumberNotLatest and " +
      "no transaction id cookie exists" in new TestWithApplication {
      import webserviceclients.audit2.AuditRequest
      val trackingId = TrackingId("default_test_tracking_id")

      val (vehicleLookup, dateService, auditService) = vehicleLookupAndAuditStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
      )
      val expected = new AuditRequest(
        name = "VehicleLookupToVehicleLookupFailure",
        serviceType = "PR Retention",
        data = Seq( ("transactionId", TransactionIdValid),
        ("timestamp", dateService.dateTimeISOChronology),
        ("rejectionCode", RecordMismatch.code + " - " + RecordMismatch.message),
        ("currentVrm", RegistrationNumberWithSpaceValid))
      )
      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService)
      val result = vehicleLookup.submit(request)

      whenReady(result) { r =>
        verify(auditService, times(1)).send(expected, trackingId = trackingId)
      }
    }

    "call audit service with 'default_test_tracking_id' when Postcodes don't match and " +
      "no transaction id cookie exists" in new TestWithApplication {
      import webserviceclients.audit2.AuditRequest
      val (vehicleLookup, dateService, auditService) = vehicleLookupAndAuditStubs()
      val trackingId = TrackingId("default_test_tracking_id")
      val expected = new AuditRequest(
        name = "VehicleLookupToVehicleLookupFailure",
        serviceType = "PR Retention",
        data =  Seq( ("transactionId", TransactionIdValid),
        ("timestamp", dateService.dateTimeISOChronology),
        ("rejectionCode", "PR002 - vehicle_and_keeper_lookup_keeper_postcode_mismatch"),
        ("currentVrm", RegistrationNumberWithSpaceValid),
        ("make", VehicleMakeValid.get),
        ("model", VehicleModelValid.get),
        ("keeperName", "MR DAVID JONES"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, SWANSEA, SA1 1AA"))
      )
      val request = buildCorrectlyPopulatedRequest(postcode = PostcodeInvalid)
      val result = vehicleLookup.submit(request)

      whenReady(result) { r =>
        verify(auditService, times(1)).send(expected, trackingId = trackingId)
      }
    }
  }

  "back" should {
    "redirect to Before You Start page when back button is pressed" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleLookupStubs().back(request)

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private def present = {
    val request = FakeRequest()
    vehicleLookupStubs().present(request)
  }

  private def vehicleLookupStubs(isPrototypeBannerVisible: Boolean = true,
                                 permitted: Boolean = true,
                                 vehicleAndKeeperLookupStatusAndResponse:
                                 (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                     VehicleAndKeeperLookupSuccessResponse]])
                                                                          = vehicleAndKeeperDetailsResponseSuccess) = {
    testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new TestVehicleAndKeeperLookupWebService(statusAndResponse = vehicleAndKeeperLookupStatusAndResponse),
      new EligibilityWebServiceCallWithResponse()
    ).getInstance(classOf[VehicleLookup])
  }

  private def vehicleLookupStubs = {
    val vehicleAndKeeperLookupWebService = new TestVehicleAndKeeperLookupWebService(statusAndResponse
      = vehicleAndKeeperDetailsResponseSuccess)
    val injector = testInjector(
      new TestBruteForcePreventionWebService(permitted = true),
      new TestConfig(isPrototypeBannerVisible = true),
      vehicleAndKeeperLookupWebService,
      new EligibilityWebServiceCallWithResponse()
    )
    (injector.getInstance(classOf[VehicleLookup]),
      injector.getInstance(classOf[DateService]),
      vehicleAndKeeperLookupWebService.stub
    )
  }

  private def vehicleLookupAndAuditStubs(isPrototypeBannerVisible: Boolean = true,
                                         permitted: Boolean = true,
                                         vehicleAndKeeperLookupStatusAndResponse:
                                         (Int, Option[Either[VehicleAndKeeperLookupFailureResponse,
                                                             VehicleAndKeeperLookupSuccessResponse]])
                                                                          = vehicleAndKeeperDetailsResponseSuccess) = {
    val ioc = testInjector(
      new TestBruteForcePreventionWebService(permitted = permitted),
      new TestConfig(isPrototypeBannerVisible = isPrototypeBannerVisible),
      new TestVehicleAndKeeperLookupWebService(statusAndResponse = vehicleAndKeeperLookupStatusAndResponse),
      new EligibilityWebServiceCallWithResponse()
    )
    (ioc.getInstance(classOf[VehicleLookup]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid,
                                             postcode: String = PostcodeValid,
                                             KeeperConsent: String = KeeperConsentValid) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber,
      PostcodeId -> postcode,
      KeeperConsentId -> KeeperConsent)
  }

  private def vehicleAndKeeperLookupCallFails = {
    testInjector(
      new VehicleAndKeeperLookupCallFails()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallNoResponse = {
    testInjector(
      new VehicleAndKeeperLookupCallNoResponse()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallServerDown = {
    testInjector(
      new VehicleAndKeeperDetailsCallServerDown()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallDocRefNumberNotLatest = {
    testInjector(
      new VehicleAndKeeperDetailsCallDocRefNumberNotLatest()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def vehicleAndKeeperDetailsCallVRMNotFound = {
    testInjector(
      new VehicleAndKeeperDetailsCallVRMNotFound()
    ).
      getInstance(classOf[VehicleLookup])
  }

  private def buildHeader(trackingId: TrackingId, dateService: DateService): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId.value,
      originDateTime = dateService.now.toDateTime,
      applicationCode = "test-applicationCode",
      channelCode = "test-channelCode",
      contactId = 42,
      eventFlag = alwaysLog,
      serviceTypeCode = "test-dmsServiceTypeCode",
      languageCode = englishLanguage,
      endUser = None)
  }
}
