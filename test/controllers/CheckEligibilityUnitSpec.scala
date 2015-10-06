package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.WithApplication
import composition.webserviceclients.vrmretentioneligibility
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import vrmretentioneligibility.EligibilityWebServiceCallFails
import vrmretentioneligibility.EligibilityWebServiceCallWithResponse
import vrmretentioneligibility.EligibilityWebServiceCallWithCurrentAndEmptyReplacement
import vrmretentioneligibility.EligibilityWebServiceCallWithCurrentAndReplacement
import helpers.UnitSpec
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs.storeBusinessDetailsConsent
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.trackingIdModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import org.mockito.Mockito.{times, verify}
import pages.vrm_retention.ConfirmPage
import pages.vrm_retention.ErrorPage
import pages.vrm_retention.MicroServiceErrorPage
import pages.vrm_retention.SetupBusinessDetailsPage
import pages.vrm_retention.VehicleLookupFailurePage
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.audit2.AuditRequest
import webserviceclients.audit2.AuditService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.TransactionIdValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleMakeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleModelValid
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid

class CheckEligibilityUnitSpec extends UnitSpec {

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

    "redirect to VehicleLookupFailure page when web service returns with a response code" in new WithApplication {
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

    "write cookie when web service returns with a response code" in new WithApplication {
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
        cookies.map(_.name) should contain(VehicleAndKeeperLookupResponseCodeCacheKey)
      }
    }

    "redirect to Confirm page when response has empty response, " +
      "current and replacement vrm, and user type is Keeper" in new WithApplication {
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
      new WithApplication {
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
      "current and empty replacement vrm" in new WithApplication {
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

    "calls audit service with expected values when the required cookies exist" in new WithApplication {
      val (checkEligibility, dateService, auditService) = checkEligibilityAndAudit()
      val expected = new AuditRequest(
        name = "VehicleLookupToConfirm",
        serviceType = "PR Retention",
        data = Seq(
          ("transactionId", TransactionIdValid),
          ("timestamp", dateService.dateTimeISOChronology),
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

      whenReady(result, timeout) { r =>
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