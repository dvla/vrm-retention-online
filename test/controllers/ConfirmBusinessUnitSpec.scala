package controllers

import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{MockFeedbackPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import views.vrm_retention.VehicleLookup._
import helpers.common.CookieHelper._
import scala.Some

final class ConfirmBusinessUnitSpec extends UnitSpec {

//  "present" should {
//
//    "display the page when required cookies are cached" in new WithApplication {
//      whenReady(present, timeout) { r =>
//        r.header.status should equal(OK)
//      }
//    }
//
//    "redirect to VehicleLookup when required cookies do not exist" in new WithApplication {
//      val request = FakeRequest()
//      val result = confirmBusiness.present(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
//      }
//    }
//
//    "display a summary of previously entered user data" in new WithApplication {
//      val content = contentAsString(present)
//      content should include(BusinessAddressLine1Valid)
//      content should include(BusinessAddressLine2Valid)
//      content should include(BusinessAddressPostTownValid)
//      content should include(RegistrationNumberValid)
//      content should include(VehicleMakeValid.get)
//      content should include(VehicleModelValid.get)
//    }
//  }
//
//  "submit" should {
//
//
//    "write StoreBusinessDetails cookie when user type is Business and has not provided a keeperEmail" in new WithApplication {
//      val request = buildRequest(keeperEmail = "").
//        withCookies(
//          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
//          vehicleAndKeeperDetailsModel(),
//          businessDetailsModel(),
//          keeperEmail(),
//          transactionId(),
//          eligibilityModel()
//        )
//      val result = confirm.submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
//      }
//    }
//
//    "write StoreBusinessDetails cookie with maxAge 7 days" in new WithApplication {
//      val expected = 7.days.toSeconds.toInt
//      val request = buildRequest(keeperEmail = "").
//        withCookies(
//          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
//          vehicleAndKeeperDetailsModel(),
//          businessDetailsModel(),
//          keeperEmail(),
//          transactionId(),
//          eligibilityModel()
//        )
//      val result = confirmWithCookieFlags.submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.find(cookie => cookie.name == StoreBusinessDetailsCacheKey).get.maxAge should equal(Some(expected))
//      }
//    }
//
//    "write StoreBusinessDetails cookie when user type is Business and has provided a keeperEmail" in new WithApplication {
//      val request = buildRequest().
//        withCookies(
//          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
//          vehicleAndKeeperDetailsModel(),
//          businessDetailsModel(),
//          keeperEmail(),
//          transactionId(),
//          eligibilityModel()
//        )
//      val result = confirm.submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain allOf(KeeperEmailCacheKey, StoreBusinessDetailsCacheKey)
//      }
//    }
//  }
//
//
//  "exit" should {
//
//    "redirect to mock feedback page" in new WithApplication {
//      val request = FakeRequest()
//      val result = confirmBusiness.exit(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
//      }
//    }
//  }
//
//
//
//  private def buildRequest(storeDetailsConsent: Boolean = false) = {
//    FakeRequest().withFormUrlEncodedBody(
//      StoreDetailsConsentId -> storeDetailsConsent
//    )
//  }
//
//
//  private def confirmBusiness = testInjector().getInstance(classOf[ConfirmBusiness])
//
//  private def present = {
//    val request = FakeRequest().
//      withCookies(
//        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
//        vehicleAndKeeperDetailsModel(),
//        businessDetailsModel()
//      )
//    confirmBusiness.present(request)
//  }
//
//  private def confirmWithCookieFlags = {
//    testInjector(new ScalaModule() {
//      override def configure(): Unit = {
//        bind[CookieFlags].to[CookieFlagsRetention].asEagerSingleton()
//      }
//    }).getInstance(classOf[Confirm])
//  }
}