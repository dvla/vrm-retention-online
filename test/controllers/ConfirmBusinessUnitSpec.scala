package controllers

import composition.TestDateService
import helpers.TestWithApplication
import composition.webserviceclients.audit2.AuditServiceDoesNothing
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_retention.CookieFactoryForUnitSpecs.storeBusinessDetailsConsent
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.trackingIdModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import org.mockito.Mockito.verify
import pages.vrm_retention.{SetupBusinessDetailsPage, LeaveFeedbackPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import scala.concurrent.duration.DurationInt
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.UserType_Business
import webserviceclients.audit2.AuditRequest
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressLine1Valid
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressLine2Valid
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressLine3Valid
import webserviceclients.fakes.AddressLookupServiceConstants.BusinessAddressPostTownValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleMakeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleModelValid

class ConfirmBusinessUnitSpec extends UnitSpec {

  "present" should {
    "display the page when required cookies are cached" in new TestWithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setupBusinessDetailsPage when required cookies do not exist" in new TestWithApplication {
      val request = FakeRequest()
      val result = confirmBusiness.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupBusinessDetailsPage.address))
      }
    }

    "display a summary of previously entered user data" in new TestWithApplication {
      val content = contentAsString(present)
      content should include(BusinessAddressLine1Valid)
      content should include(BusinessAddressLine2Valid)
      content should include(BusinessAddressLine3Valid)
      content should include(BusinessAddressPostTownValid)
      content should include(RegistrationNumberValid)
      content should include(VehicleMakeValid.get)
      content should include(VehicleModelValid.get)
    }
  }

  "submit" should {
    "call the audit service" in new TestWithApplication {
      val auditService2 = new AuditServiceDoesNothing

      val injector = testInjector(
        auditService2
      )

      val confirmBusiness = injector.getInstance(classOf[ConfirmBusiness])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("documentReferenceNumber", ReferenceNumberValid),
        ("replacementVrm", "SA11AA"),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "Mr David Jones"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress",
          "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@test.com"))
      val auditRequest = new AuditRequest(
        AuditRequest.ConfirmBusinessToConfirm,
        AuditRequest.PersonalisedRegServiceType,
        data)
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel(),
          trackingIdModel()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        verify(auditService2.stub).send(auditRequest, TrackingId("trackingId"))
      }
    }

    "refresh all of the business details cookies to have a maxAge that is 7 days " +
      "in the future if user is a business" in new TestWithApplication {
      val expected = 7.days.toSeconds.toInt
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          transactionId(),
          eligibilityModel(),
          businessDetailsModel(),
          setupBusinessDetails(),
          storeBusinessDetailsConsent()
        )

      val result = confirmBusiness.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BusinessDetailsCacheKey,
          SetupBusinessDetailsCacheKey
          )
        cookies.find(_.name == BusinessDetailsCacheKey).get.maxAge.get === expected +- 1
        cookies.find(_.name == SetupBusinessDetailsCacheKey).get.maxAge.get === expected +- 1
      }
    }
  }

  "back" should {
    "redirect to SetupBusinessDetails page when navigating back" in new TestWithApplication {
      val request = FakeRequest()
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
    "redirect to mock feedback page" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
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

  private def confirmBusiness = testInjector(new TestDateService).getInstance(classOf[ConfirmBusiness])

  private def present = {
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
        vehicleAndKeeperDetailsModel(),
        setupBusinessDetails(),
        businessDetailsModel()
      )
    confirmBusiness.present(request)
  }
}
