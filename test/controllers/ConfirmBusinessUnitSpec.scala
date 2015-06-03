package controllers

import composition.TestDateService
import composition.WithApplication
import composition.webserviceclients.audit2.AuditServiceDoesNothing
import helpers.UnitSpec
import helpers.common.CookieHelper._
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import org.mockito.Mockito._
import pages.vrm_retention.{SetupBusinessDetailsPage, LeaveFeedbackPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import scala.concurrent.duration.DurationInt
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.ConfirmBusiness._
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2.AuditRequest
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._

class ConfirmBusinessUnitSpec extends UnitSpec {

  "present" should {

    "display the page when required cookies are cached" in new WithApplication {
      whenReady(present, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setupBusinessDetailsPage when required cookies do not exist" in new WithApplication {
      val request = FakeRequest()
      val result = confirmBusiness.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display a summary of previously entered user data" in new WithApplication {
      val content = contentAsString(present)
      content should include(BusinessAddressLine1Valid)
      content should include(BusinessAddressLine2Valid)
      content should include(BusinessAddressPostTownValid)
      content should include(RegistrationNumberValid)
      content should include(VehicleMakeValid.get)
      content should include(VehicleModelValid.get)
    }
  }

  "submit" should {
    // TODO: ian restore these tests
/*
    "write StoreBusinessDetails cookie when user type is Business and consent is true" in new WithApplication {
      val injector = testInjector()
      val confirmBusiness = injector.getInstance(classOf[ConfirmBusiness])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("replacementVrm", "SA11AA"),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "Mr David Jones"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress", "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@test.com"))
      val request = buildRequest(storeDetailsConsent = true).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
      }
    }

    "write StoreBusinessDetails cookie with a maxAge 7 days in the future" in new WithApplication {
      val expected = 7.days.toSeconds.toInt
      val request = buildRequest(storeDetailsConsent = true).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId(),
          eligibilityModel(),
          storeBusinessDetailsConsent()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
        cookies.find(cookie => cookie.name == StoreBusinessDetailsCacheKey).get.maxAge.get === expected +- 1
      }
    }

    "write StoreBusinessDetails cookie when user type is Business and consent is false" in new WithApplication {
      val request = buildRequest(storeDetailsConsent = false).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirmBusiness.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
      }
    }

    "call the audit service" in new WithApplication {
      val auditService2 = new AuditServiceDoesNothing

      val injector = testInjector(
        auditService2
      )

      val confirmBusiness = injector.getInstance(classOf[ConfirmBusiness])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("replacementVrm", "SA11AA"),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "Mr David Jones"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress", "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@test.com"))
      val auditRequest = new AuditRequest(AuditRequest.ConfirmBusinessToConfirm, AuditRequest.PersonalisedRegServiceType, data)
      val request = buildRequest(storeDetailsConsent = true).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
        verify(auditService2.stub).send(auditRequest)
      }
    }

    "refresh all of the business details cookies to have a maxAge that is 7 days in the future if user is a business" in new WithApplication {
      val expected = 7.days.toSeconds.toInt
      val request = buildRequest(storeDetailsConsent = true).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          transactionId(),
          eligibilityModel(),
          businessChooseYourAddress(),
          businessDetailsModel(),
          setupBusinessDetails(),
          storeBusinessDetailsConsent()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BusinessChooseYourAddressCacheKey,
          BusinessDetailsCacheKey,
          SetupBusinessDetailsCacheKey
          )
        cookies.find(_.name == BusinessChooseYourAddressCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == BusinessDetailsCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == SetupBusinessDetailsCacheKey).get.maxAge.get === expected +- 1
      }
    }

    "refresh all of the business details cookies to have a maxAge that is 7 days in the future if user is a business and entered address manually" in new WithApplication {
      val expected = 7.days.toSeconds.toInt
      val request = buildRequest(storeDetailsConsent = true).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          transactionId(),
          eligibilityModel(),
          enterAddressManually(),
          businessChooseYourAddress(),
          businessDetailsModel(),
          setupBusinessDetails(),
          storeBusinessDetailsConsent()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          EnterAddressManuallyCacheKey,
          BusinessChooseYourAddressCacheKey,
          BusinessDetailsCacheKey,
          SetupBusinessDetailsCacheKey
          )
        cookies.find(_.name == EnterAddressManuallyCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == BusinessChooseYourAddressCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == BusinessDetailsCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == SetupBusinessDetailsCacheKey).get.maxAge.get === expected +- 1
      }
    }
*/
  }

  "back" should {
    "redirect to SetupBusinessDetails page when navigating back" in new WithApplication {
      val request = buildRequest(storeDetailsConsent = false)
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirmBusiness.back(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }
  }

  "exit" should {
    "redirect to mock feedback page" in new WithApplication {
      val request = buildRequest(storeDetailsConsent = false).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirmBusiness.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  private def buildRequest(storeDetailsConsent: Boolean = false) = {
    FakeRequest().withFormUrlEncodedBody(
      StoreDetailsConsentId -> storeDetailsConsent.toString
    )
  }

  private def confirmBusiness = testInjector(new TestDateService).getInstance(classOf[ConfirmBusiness])

  private def present = {
    val request = FakeRequest().
      withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
        vehicleAndKeeperDetailsModel(),
        setupBusinessDetails(),
        businessDetailsModel()
      )
    confirmBusiness.present(request)
  }
}