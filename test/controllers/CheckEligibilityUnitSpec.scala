package controllers

import audit.{AuditMessage, AuditService}
import com.tzavellas.sse.guice.ScalaModule
import composition.eligibility._
import composition.{TestAuditService, TestDateService}
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito._
import pages.vrm_retention.{ConfirmPage, ErrorPage, MicroServiceErrorPage, SetupBusinessDetailsPage, VehicleLookupFailurePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants._

final class CheckEligibilityUnitSpec extends UnitSpec {

  "present" should {

    "redirect to error page when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
      val result = checkEligibility().present(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when VehicleAndKeeperDetailsModel cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel())
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when StoreBusinessDetailsCacheKey cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel()
        )
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when TransactionIdCacheKey cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent()
        )
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to micro-service error page when web service call fails" in new WithApplication {
      val request = FakeRequest().
        withCookies(
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

    "redirect to VehicleLookupFailure page when web service returns with a response code" in new WithApplication {
      val request = FakeRequest().
        withCookies(
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

    "write cookie when web service returns with a response code" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallWithResponse()).present(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(VehicleAndKeeperLookupResponseCodeCacheKey)
      }
    }

    "redirect to Confirm page when response has empty response, current and replacement vrm, and user type is Keeper" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = KeeperConsentValid),
          vehicleAndKeeperDetailsModel(),
          transactionId()
        )
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
      }
    }

    "redirect to SetUpBusinessDetails page when response has empty response, current and replacement vrm, and user type is Business" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          transactionId()
        )
      val result = checkEligibility().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "redirect to MicroServiceError page when response has empty response, empty current and empty replacement vrm" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallWithEmptyCurrentAndEmptyReplacement()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when response has empty response, current and empty replacement vrm" in new WithApplication {
      val request = FakeRequest().
        withCookies(
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

    "redirect to MicroServiceError page when response has empty response, empty current and replacement vrm" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibility(new EligibilityWebServiceCallWithEmptyCurrentAndReplacement()).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "calls audit service with expected values when transaction id cookie exists" in new WithApplication {
      val (checkEligibility, dateService, auditService) = checkEligibilityAndAudit()
      val expected = new AuditMessage(
        name = "VehicleLookupToConfirm",
        serviceType = "PR Retention",
        ("transactionId", TransactionIdValid),
        ("timestamp", dateService.dateTimeISOChronology),
        ("replacementVrm", ReplacementRegistrationNumberValid),
        ("currentVrm", RegistrationNumberValid),
        ("make", VehicleMakeValid.get),
        ("model", VehicleModelValid.get),
        ("keeperName", "Mr David Jones"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA")
      )
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = KeeperConsentValid),
          vehicleAndKeeperDetailsModel(),
          transactionId()
        )
      val result = checkEligibility.present(request)

      whenReady(result, timeout) { r =>
        verify(auditService, times(1)).send(expected)
      }
    }
  }

  private def checkEligibility(eligibilityWebService: ScalaModule = new EligibilityWebServiceCallWithCurrentAndReplacement()) = {
    testInjector(
      new TestAuditService(),
      eligibilityWebService
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityAndAudit(eligibilityWebService: ScalaModule = new EligibilityWebServiceCallWithCurrentAndReplacement()) = {
    val auditService = mock[AuditService]
    val ioc = testInjector(
      new TestAuditService(auditService = auditService),
      new TestDateService(),
      eligibilityWebService
    )
    (ioc.getInstance(classOf[CheckEligibility]), ioc.getInstance(classOf[DateService]), ioc.getInstance(classOf[AuditService]))
  }
}