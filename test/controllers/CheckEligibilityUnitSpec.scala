package controllers

import composition.eligibility.{EligibilityWebServiceCallWithCurrentAndReplacement, EligibilityWebServiceCallFails, EligibilityWebServiceCallWithResponse}
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{ConfirmPage, ErrorPage, MicroServiceErrorPage, VehicleLookupFailurePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperConsentValid

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

    "redirect to Confirm page when response has current and replacement vrm, and user type is keeper" in new WithApplication {
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
  }

  private lazy val checkEligibility = testInjector().getInstance(classOf[CheckEligibility])

  private def checkEligibilityCallFails = {
    testInjector(
      new EligibilityWebServiceCallFails()
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityWithResponse = {
    testInjector(
      new EligibilityWebServiceCallWithResponse()
    ).
      getInstance(classOf[CheckEligibility])
  }

  private def checkEligibilityWithCurrentAndReplacement = {
    testInjector(
      new EligibilityWebServiceCallWithCurrentAndReplacement()
    ).
      getInstance(classOf[CheckEligibility])
  }
}