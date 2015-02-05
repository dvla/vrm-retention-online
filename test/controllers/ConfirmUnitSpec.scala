package controllers

import audit1.AuditMessage
import composition.WithApplication
import composition.audit1.AuditLocalService
import helpers.UnitSpec
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import org.mockito.Mockito.verify
import pages.vrm_retention.{PaymentPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_retention.Confirm._
import views.vrm_retention.VehicleLookup.{UserType_Business, UserType_Keeper}
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid

final class ConfirmUnitSpec extends UnitSpec {

  "present" should {

    "display the page when required cookies are cached" in new WithApplication {
      whenReady(present, timeout) {
        r =>
          r.header.status should equal(OK)
      }
    }

    "display the page when required cookies are cached and StoreBusinessDetails cookie exists and is true" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          storeBusinessDetailsConsent()
        )
      val result = confirm.present(request)
      whenReady(result, timeout) {
        r =>
          r.header.status should equal(OK)
      }
    }

    "redirect to VehicleLookup when required cookies do not exist" in new WithApplication {
      val request = FakeRequest()
      val result = confirm.present(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  "submit" should {

    "redirect to Payment page when valid submit and user type is Business" in new WithApplication {
      val auditService1 = new AuditLocalService

      val injector = testInjector(
        auditService1
      )

      val confirm = injector.getInstance(classOf[Confirm])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("replacementVrm", "SA11AA"),
        ("keeperEmail", "example@email.com"),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "Mr David Jones"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress", "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@email.com"))
      val auditMessage = new AuditMessage(AuditMessage.ConfirmToPayment, AuditMessage.PersonalisedRegServiceType, data: _*)

      val request = buildRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirm.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(PaymentPage.address))
          verify(auditService1.stub).send(auditMessage)
      }
    }

    "redirect to Payment page when valid submit and user type is Keeper" in new WithApplication {
      val request = buildRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirm.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(PaymentPage.address))
      }
    }

    "not write cookies when user type is Keeper and has not provided a keeperEmail" in new WithApplication {
      val request = buildRequest(keeperEmail = "").
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirm.submit(request)
      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should be(empty)
      }
    }

    "write ConfirmFormModel cookie when user type is Keeper and has provided a keeperEmail" in new WithApplication {
      val request = buildRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          confirmFormModel(),
          transactionId(),
          eligibilityModel()
        )
      val result = confirm.submit(request)
      whenReady(result) {
        r =>
          val cookies = fetchCookiesFromHeaders(r)
          cookies.map(_.name) should contain(ConfirmCacheKey)
      }
    }

    "return a bad request when the supply email field has nothing selected" in new WithApplication {
      val request = buildRequest(supplyEmail = supplyEmailEmpty).
        withCookies(
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

    "return a bad request when the keeper wants to supply an email and does not provide an email address" in new WithApplication {
      val request = buildRequest(keeperEmail = keeperEmailEmpty).
        withCookies(
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

  private def present = {
    val request = FakeRequest().
      withCookies(
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel()
      )
    confirm.present(request)
  }

  private def confirm = testInjector().getInstance(classOf[Confirm])

  private val supplyEmailTrue = "true"
  private val supplyEmailEmpty = ""
  private val keeperEmailEmpty = ""

  private def buildRequest(keeperEmail: String = KeeperEmailValid.get, supplyEmail: String = supplyEmailTrue) = {
    FakeRequest().withFormUrlEncodedBody(
      KeeperEmailId -> keeperEmail,
      SupplyEmailId -> supplyEmail
    )
  }
}