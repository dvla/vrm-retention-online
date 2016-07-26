package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vrmretentioneligibility.{EligibilityWebServiceCallWithSensitiveResponse, EligibilityWebServiceCallFails, EligibilityWebServiceCallWithCurrentAndEmptyReplacement, EligibilityWebServiceCallWithCurrentAndReplacement, EligibilityWebServiceCallWithResponse}
import helpers.JsonUtils.deserializeJsonToModel
import helpers.{UnitSpec, TestWithApplication}
import helpers.vrm_retention.CookieFactoryForUnitSpecs.{storeBusinessDetailsConsent, trackingIdModel, transactionId, vehicleAndKeeperDetailsModel}
import helpers.vrm_retention.CookieFactoryForUnitSpecs.{vehicleAndKeeperLookupFormModel}
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel
import org.mockito.Mockito.{times, verify}
import pages.vrm_retention.{ConfirmPage, ErrorPage, MicroServiceErrorPage, SetupBusinessDetailsPage, VehicleLookupFailurePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel.MsResponseCacheKey
import webserviceclients.audit2.{AuditRequest, AuditService}
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.{BusinessConsentValid, KeeperConsentValid, RegistrationNumberValid, ReferenceNumberValid}
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.{TransactionIdValid, VehicleMakeValid, VehicleModelValid}
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid

class CheckEligibilityUnitSpec extends UnitSpec {

  "present" should {
    "redirect to error page when VehicleAndKeeperLookupFormModel cookie does not exist" in new TestWithApplication {
      val result = checkEligibility().present(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when VehicleAndKeeperDetailsModel cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel())
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when StoreBusinessDetailsCacheKey cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel()
        )
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when TransactionIdCacheKey cookie does not exist" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent()
        )
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to micro-service error page when web service call fails" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallFails()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to VehicleLookupFailure page when web service returns with a response code" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallWithResponse()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupFailurePage.address))
      }
    }

    "write cookie when web service returns with a response code" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallWithResponse()).present(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(MsResponseCacheKey)

        val cookieName = MsResponseCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[MicroserviceResponseModel](json)
              model.msResponse.code should equal("nonsensitive")
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "write cookie when web service returns with a sensitive response code" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallWithSensitiveResponse()).present(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(MsResponseCacheKey)

        val cookieName = MsResponseCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[MicroserviceResponseModel](json)
              model.msResponse.code should equal("")
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "redirect to Confirm page when response has empty response, " +
      "current and replacement vrm, and user type is Keeper" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = KeeperConsentValid),
          vehicleAndKeeperDetailsModel(),
          transactionId()
        )
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
      }
    }

    "redirect to SetUpBusinessDetails response has empty response, " +
      "current and replacement vrm, and user type is Business" in
      new TestWithApplication {
        val request = FakeRequest()
          .withCookies(
            vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
            vehicleAndKeeperDetailsModel(),
            transactionId()
          )
        val result = checkEligibility().present(request)
        whenReady(result) { r =>
          r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
        }
    }

    "redirect to MicroServiceError page when response has empty response, " +
      "current and empty replacement vrm" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallWithCurrentAndEmptyReplacement()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "calls audit service with expected values when the required cookies exist" in new TestWithApplication {
      val (checkEligibility, dateService, auditService) = checkEligibilityAndAudit()
      val expected = new AuditRequest(
        name = "VehicleLookupToConfirm",
        serviceType = "PR Retention",
        data = Seq(
          ("transactionId", TransactionIdValid),
          ("timestamp", dateService.dateTimeISOChronology),
          ("documentReferenceNumber", ReferenceNumberValid),
          ("replacementVrm", ReplacementRegistrationNumberValid),
          ("currentVrm", RegistrationNumberValid),
          ("make", VehicleMakeValid.get),
          ("model", VehicleModelValid.get),
          ("keeperName", "Mr David Jones"),
          ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA")
        )
      )
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = KeeperConsentValid),
          vehicleAndKeeperDetailsModel(),
          transactionId(),
          trackingIdModel()
        )
      val result = checkEligibility.present(request)

      whenReady(result) { r =>
        verify(auditService, times(1)).send(expected, TrackingId("trackingId"))
      }
    }
  }

  private def checkEligibility(eligibilityWebService: ScalaModule =
                               new EligibilityWebServiceCallWithCurrentAndReplacement()) = {
    testInjector(
      eligibilityWebService
    ).getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityAndAudit(eligibilityWebService: ScalaModule =
                                       new EligibilityWebServiceCallWithCurrentAndReplacement()) = {
    val ioc = testInjector(
      eligibilityWebService
    )
    (
      ioc.getInstance(classOf[CheckEligibility]),
      ioc.getInstance(classOf[DateService]),
      ioc.getInstance(classOf[AuditService])
    )
  }
}
