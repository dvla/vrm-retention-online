package controllers

import composition.TestAuditService
import composition.eligibility._
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{ConfirmPage, ErrorPage, MicroServiceErrorPage, VehicleLookupFailurePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.{BusinessConsentValid, KeeperConsentValid}

final class CheckEligibilityUnitSpec extends UnitSpec {

  "present" should {

    "redirect to error page when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
      val result = checkEligibility.present(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when VehicleAndKeeperDetailsModel cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel())
      val result = checkEligibility.present(request)
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
      val result = checkEligibility.present(request)
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
      val result = checkEligibility.present(request)
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
      val result = checkEligibilityCallFails.present(request)
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
      val result = checkEligibilityWithResponse.present(request)
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
      val result = checkEligibilityWithResponse.present(request)
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
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibilityWithCurrentAndReplacement.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
      }
    }

    "redirect to SetUpBusinessDetails page when response has empty response, current and replacement vrm, and user type is Business" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent(),
          transactionId()
        )
      val result = checkEligibilityWithCurrentAndReplacement.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmPage.address))
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
      val result = checkEligibilityWithEmptyCurrentAndEmptyReplacement.present(request)
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
      val result = checkEligibilityWithCurrentAndEmptyReplacement.present(request)
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
      val result = checkEligibilityWithEmptyCurrentAndReplacement.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }
  }

  private lazy val checkEligibility = testInjector().getInstance(classOf[CheckEligibility])

  private def checkEligibilityCallFails = {
    testInjector(
      new TestAuditService(),
      new EligibilityWebServiceCallFails()
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityWithResponse = {
    testInjector(
      new TestAuditService(),
      new EligibilityWebServiceCallWithResponse()
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityWithCurrentAndReplacement = {
    testInjector(
      new TestAuditService(),
      new EligibilityWebServiceCallWithCurrentAndReplacement()
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityWithEmptyCurrentAndEmptyReplacement = {
    testInjector(
      new TestAuditService(),
      new EligibilityWebServiceCallWithEmptyCurrentAndEmptyReplacement()
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityWithCurrentAndEmptyReplacement = {
    testInjector(
      new TestAuditService(),
      new EligibilityWebServiceCallWithCurrentAndEmptyReplacement()
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityWithEmptyCurrentAndReplacement = {
    testInjector(
      new TestAuditService(),
      new EligibilityWebServiceCallWithEmptyCurrentAndReplacement()
    ).
      getInstance(classOf[CheckEligibility])
  }
}