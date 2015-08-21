package controllers

import _root_.webserviceclients.audit2.AuditService
import _root_.webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.VrmLocked
import _root_.webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants
import composition.TestConfig
import composition.webserviceclients.bruteforceprevention.TestBruteForcePreventionWebService
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallDocRefNumberNotLatest
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallServerDown
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsCallVRMNotFound
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupCallFails
import composition.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupCallNoResponse
import composition.webserviceclients.vrmretentioneligibility.EligibilityWebServiceCallWithResponse
import composition.WithApplication
import controllers.Common.PrototypeHtml
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import org.mockito.Mockito.verify
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.CheckEligibilityPage
import pages.vrm_retention.MicroServiceErrorPage
import pages.vrm_retention.VehicleLookupFailurePage
import pages.vrm_retention.VrmLockedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.mappings.DocumentReferenceNumber
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.DmsWebHeaderDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupResponse
import VehicleAndKeeperLookupWebServiceConstants.KeeperConsentValid
import VehicleAndKeeperLookupWebServiceConstants.KeeperPostcodeValidForMicroService
import VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid
import VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberWithSpaceValid
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseDamagedFailure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseExportedFailure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseNoKeeperFailure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseNotFoundResponseCode
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseNotMotFailure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponsePre1998Failure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseQFailure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseScrappedFailure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseSuccess
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseVICFailure
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseVRMNotFound
import VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsServerDown
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.VehicleLookup.DocumentReferenceNumberId
import views.vrm_retention.VehicleLookup.KeeperConsentId
import views.vrm_retention.VehicleLookup.PostcodeId
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import views.vrm_retention.VehicleLookup.VehicleRegistrationNumberId

class VehicleLookupUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
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

  "submit" should {
    "redirect to CheckEligibility controller after a valid submit and " +
      "true message returned from the fake microservice" in new WithApplication {
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

    "submit removes spaces from registrationNumber" in new WithApplication {
      // DE7 Spaces should be stripped
      val request = buildCorrectlyPopulatedRequest(registrationNumber = RegistrationNumberWithSpaceValid)
      val result = vehicleLookupStubs().submit(request)

      whenReady(result, timeout) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "redirect to MicroServiceError after a submit and no response code and " +
      "no vehicledetailsdto returned from the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseNotFoundResponseCode).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vrm not found by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseVRMNotFound).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "document reference number mismatch returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseDocRefNumberNotLatest).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseExportedFailure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseExportedFailure).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseScrappedFailure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result =
        vehicleLookupStubs(vehicleAndKeeperLookupStatusAndResponse =
          vehicleAndKeeperDetailsResponseScrappedFailure).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseDamagedFailure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDamagedFailure
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseVICFailure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseVICFailure
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseNoKeeperFailure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseNoKeeperFailure
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseNotMotFailure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseNotMotFailure
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponsePre1998Failure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponsePre1998Failure
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vehicleAndKeeperDetailsResponseQFailure returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseQFailure
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure after a submit and " +
      "vss error returned by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsServerDown
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "replace max length error message for document reference number with " +
      "standard error message (US43)" in new WithApplication {
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
      "standard error message (US43)" in new WithApplication {
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
      "standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration mark with " +
      "standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "")
      val result = vehicleLookupStubs().submit(request)
      val count = "Must be valid format".r.findAllIn(contentAsString(result)).length
      // The same message is displayed in 2 places -
      // once in the validation-summary at the top of the page and once above the field.
      count should equal(2)
    }

    "redirect to MicroserviceError page when vehicleAndKeeperLookup throws an exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails.submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "does not write VehicleAndKeeperDetailsModel cookie when microservice throws an exception" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperLookupCallFails.submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should not contain vehicleAndKeeperLookupDetailsCacheKey
      }
    }

    "redirect to MicroServiceError after a submit if response status is Ok and " +
      "no response payload" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallNoResponse.submit(request)

      whenReady(result, timeout) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "write cookie when vss error returned by the microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallServerDown.submit(request)

      whenReady(result, timeout) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "write cookie when vrm not found by the fake microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallVRMNotFound.submit(request)
      whenReady(result, timeout) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain allOf(
            PaymentTransNoCacheKey, TransactionIdCacheKey, bruteForcePreventionViewModelCacheKey,
            VehicleAndKeeperLookupResponseCodeCacheKey, VehicleAndKeeperLookupFormModelCacheKey)
      }
    }

    "redirect to vrm locked when valid submit and brute force prevention returns not permitted" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = VrmLocked)
      val result = vehicleLookupStubs(permitted = false).submit(request)
      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VrmLockedPage.address))
      }
    }

    "redirect to VehicleAndKeeperLookupFailure and " +
      "display 1st attempt message when document reference number not found and " +
      "security service returns 1st attempt" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest.submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "write cookie when document reference number mismatch returned by microservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleAndKeeperDetailsCallDocRefNumberNotLatest.submit(request)
      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain allOf(
            bruteForcePreventionViewModelCacheKey,
            VehicleAndKeeperLookupResponseCodeCacheKey,
            VehicleAndKeeperLookupFormModelCacheKey
          )
      }
    }

    "redirect to VehicleAndKeeperLookupFailure and " +
      "display 2nd attempt message when document reference number not found and " +
      "security service returns 2nd attempt" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(
        registrationNumber = BruteForcePreventionWebServiceConstants.VrmAttempt2
      )
      val result = vehicleLookupStubs(
        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
      ).submit(request)

      whenReady(result, timeout) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "send a request and a trackingId to the vehicleAndKeeperLookupWebService" in new WithApplication {
      val trackingId = TrackingId("default_test_tracking_id")
      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService).
        withCookies(CookieFactoryForUnitSpecs.trackingIdModel(trackingId))
      val (vehicleLookup, dateService, vehicleAndKeeperLookupWebService) = vehicleLookupStubs
      val result = vehicleLookup.submit(request)

      whenReady(result, timeout) {
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

//    "send a request and default trackingId to the vehicleAndKeeperLookupWebService when " +
//      "cookie does not exist" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService)
//      val (vehicleLookup, dateService, vehicleAndKeeperLookupWebService) = vehicleLookupStubs
//      val result = vehicleLookup.submit(request)
//
//      whenReady(result, timeout) {
//        r =>
//          val expectedRequest = VehicleAndKeeperDetailsRequest(
//            dmsHeader = buildHeader(ClearTextClientSideSessionFactory.DefaultTrackingId, dateService),
//            referenceNumber = ReferenceNumberValid,
//            registrationNumber = RegistrationNumberValid,
//            transactionTimestamp = dateService.now.toDateTime
//          )
//          verify(vehicleAndKeeperLookupWebService).invoke(
//            request = expectedRequest,
//            trackingId = ClearTextClientSideSessionFactory.DefaultTrackingId
//          )
//      }
//    }

//    "calls audit service with 'default_test_tracking_id' when DocRefNumberNotLatest and " +
//      "no transaction id cookie exists" in new WithApplication {
//      val (vehicleLookup, dateService, auditService) = vehicleLookupAndAuditStubs(
//        vehicleAndKeeperLookupStatusAndResponse = vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
//      )
//      val expected = new AuditMessage(
//        name = "VehicleLookupToVehicleLookupFailure",
//        serviceType = "PR Retention",
//        ("transactionId", ClearTextClientSideSessionFactory.DefaultTrackingId),
//        ("timestamp", dateService.dateTimeISOChronology),
//        ("rejectionCode", RecordMismatch),
//        ("currentVrm", RegistrationNumberWithSpaceValid)
//      )
//      val request = buildCorrectlyPopulatedRequest(postcode = KeeperPostcodeValidForMicroService)
//      val result = vehicleLookup.submit(request)
//
//      whenReady(result, timeout) { r =>
//        verify(auditService, times(1)).send(expected)
//      }
//    }

//    "calls audit service with 'default_test_tracking_id' when Postcodes don't match and " +
//      "no transaction id cookie exists" in new WithApplication {
//      val (vehicleLookup, dateService, auditService) = vehicleLookupAndAuditStubs()
//      val expected = new AuditMessage(
//        name = "VehicleLookupToVehicleLookupFailure",
//        serviceType = "PR Retention",
//        ("transactionId", ClearTextClientSideSessionFactory.DefaultTrackingId),
//        ("timestamp", dateService.dateTimeISOChronology),
//        ("rejectionCode", "PR002 - vehicle_and_keeper_lookup_keeper_postcode_mismatch"),
//        ("currentVrm", RegistrationNumberWithSpaceValid),
//        ("make", VehicleMakeValid.get),
//        ("model", VehicleModelValid.get),
//        ("keeperName", "MR DAVID JONES"),
//        ("keeperAddress", "1 HIGH STREET, SKEWEN, SWANSEA, SA1 1AA")      )
//      val request = buildCorrectlyPopulatedRequest(postcode = PostcodeInvalid)
//      val result = vehicleLookup.submit(request)
//
//      whenReady(result, timeout) { r =>
//        verify(auditService, times(1)).send(expected)
//      }
//    }
  }

  "back" should {
    "redirect to Before You Start page when back button is pressed" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleLookupStubs().back(request)

      whenReady(result, timeout) { r =>
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
                                 vehicleAndKeeperLookupStatusAndResponse: (Int, Option[VehicleAndKeeperLookupResponse])
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
                                         vehicleAndKeeperLookupStatusAndResponse: (Int, Option[VehicleAndKeeperLookupResponse])
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