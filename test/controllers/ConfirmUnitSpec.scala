package controllers

import composition.webserviceclients.audit2.AuditServiceDoesNothing
import helpers.TestWithApplication
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
import pages.vrm_retention.ConfirmBusinessPage
import pages.vrm_retention.PaymentPage
import pages.vrm_retention.VehicleLookupPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, OK, LOCATION}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import views.vrm_retention.Confirm.ConfirmCacheKey
import views.vrm_retention.Confirm.KeeperEmailId
import views.vrm_retention.Confirm.SupplyEmailId
import views.vrm_retention.Confirm.SupplyEmail_true
import views.vrm_retention.VehicleLookup.UserType_Business
import views.vrm_retention.VehicleLookup.UserType_Keeper
import webserviceclients.audit2.AuditRequest
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.KeeperConsentValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.ReferenceNumberValid

class ConfirmUnitSpec extends UnitSpec {

  "present" should {
    "display the page when required cookies are cached" in new TestWithApplication {
      whenReady(present) { r =>
          r.header.status should equal(OK)
      }
    }

    "display the page when required cookies are cached " +
      "and StoreBusinessDetails cookie exists and is true" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          businessDetailsModel(),
          storeBusinessDetailsConsent()
        )
      val result = confirm.present(request)
      whenReady(result) { r =>
          r.header.status should equal(OK)
      }
    }

    "redirect to VehicleLookup when required cookies do not exist" in new TestWithApplication {
      val request = FakeRequest()
      val result = confirm.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }
  }

  "submit" should {
    "redirect to Payment page when valid submit and user type is Business" in new TestWithApplication {
      val auditService2 = new AuditServiceDoesNothing

      val injector = testInjector(
        auditService2
      )

      val confirm = injector.getInstance(classOf[Confirm])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("documentReferenceNumber", ReferenceNumberValid),
        ("replacementVrm", "SA11AA"),
        ("keeperEmail", "keeper.example@test.com"),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "Mr David Jones"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress",
          "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@test.com"))
      val auditMessage = new AuditRequest(AuditRequest.ConfirmToPayment, AuditRequest.PersonalisedRegServiceType, data)

      val request = buildRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel(),
          trackingIdModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentPage.address))
        verify(auditService2.stub).send(auditMessage, TrackingId("trackingId"))
      }
    }

    "redirect to Payment page when valid submit and user type is Keeper" in new TestWithApplication {
      val request = buildRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentPage.address))
      }
    }

    "not write cookies when user type is Keeper and has not provided a keeperEmail" in new TestWithApplication {
      val request = buildRequest(keeperEmail = "")
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should be(empty)
      }
    }

    "write ConfirmFormModel cookie when user type is Keeper and has provided a keeperEmail" in new TestWithApplication {
      val request = buildRequest()
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(ConfirmCacheKey)
      }
    }

    "return a bad request when the supply email field has nothing selected" in new TestWithApplication {
      val request = buildRequest(supplyEmail = supplyEmailEmpty)
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )

      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return a bad request when the keeper wants to supply an email and does not provide an email address" in new TestWithApplication {
      val request = buildRequest(keeperEmail = keeperEmailEmpty)
        .withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )

      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }
  }

  "back" should {
    "redirect to Vehicle Lookup page when the user is a keeper" in new TestWithApplication {
      whenReady(back(KeeperConsentValid)) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to Confirm Business page when the user is a business" in new TestWithApplication {
      whenReady(back(BusinessConsentValid)) { r =>
        r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
      }
    }
  }

  private def present = {
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel(),
        eligibilityModel(),
        setupBusinessDetails(),
        businessDetailsModel()
      )
    confirm.present(request)
  }

  private def back(keeperConsent: String) = {
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = keeperConsent),
        vehicleAndKeeperDetailsModel()
      )
    confirm.back(request)
  }

  private def confirm = testInjector().getInstance(classOf[Confirm])

  private val supplyEmailEmpty = ""
  private val keeperEmailEmpty = ""

  private def buildRequest(keeperEmail: String = KeeperEmailValid.get, supplyEmail: String = SupplyEmail_true) = {
    FakeRequest().withFormUrlEncodedBody(
      s"$KeeperEmailId.$EmailId" -> keeperEmail,
      s"$KeeperEmailId.$EmailVerifyId" -> keeperEmail,
      KeeperEmailId -> keeperEmail,
      SupplyEmailId -> supplyEmail
    )
  }
}
