package controllers

import composition.TestEmailService
import composition.WithApplication
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.businessChooseYourAddress
import helpers.vrm_retention.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_retention.CookieFactoryForUnitSpecs.retainModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import models.BusinessDetailsModel
import models.ConfirmFormModel
import models.EligibilityModel
import models.RetainModel
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.scalatest.mock.MockitoSugar
import pages.vrm_retention.SuccessPage
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import views.vrm_retention.Confirm.SupplyEmail_false
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid

final class SuccessPaymentUnitSpec extends UnitSpec with MockitoSugar {

  "present" should {
    "display the page when BusinessDetailsModel cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          businessDetailsModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, _) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, _) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "call the email service when businessDetails cookie exists" in new WithApplication {
      val isKeeper = false
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          setupBusinessDetails(),
          businessChooseYourAddress(),
          businessDetailsModel(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(keeperEmail = None, supplyEmail = SupplyEmail_false),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, times(1)).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[EligibilityModel],
          any[RetainModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "call the email service when keeper selected to supply an email address and did supply an email" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(keeperEmail = KeeperEmailValid, supplyEmail = supplyEmailTrue),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, times(1)).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[EligibilityModel],
          any[RetainModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "not call the email service when businessDetails does not cookie" in new WithApplication {
      val isKeeper = false
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(keeperEmail = None, supplyEmail = supplyEmailTrue),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, never).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[EligibilityModel],
          any[RetainModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "not call the email service when keeper did not select to supply an email address" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(keeperEmail = None, supplyEmail = "no"),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, never).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[EligibilityModel],
          any[RetainModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }

    "not call the email service when keeper did not select to supply an email address but did provide one" in new WithApplication {
      val isKeeper = true
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(keeperEmail = KeeperEmailValid, supplyEmail = "no"),
          retainModel(),
          transactionId(),
          paymentTransNo(),
          paymentModel())
      val (successPayment, emailService) = build
      val result = successPayment.present(request)
      whenReady(result) { r =>
        verify(emailService, never).sendEmail(
          any[String],
          any[VehicleAndKeeperDetailsModel],
          any[EligibilityModel],
          any[RetainModel],
          any[String],
          any[Option[ConfirmFormModel]],
          any[Option[BusinessDetailsModel]],
          Matchers.eq(isKeeper),
          any[String]
        )
      }
    }
  }

  "create pdf" should {
    "return a bad request if cookie for EligibilityModel does no exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(transactionId())
      val (successPayment, _) = build
      val result = successPayment.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a bad request if cookie for TransactionId does no exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(eligibilityModel())
      val (successPayment, _) = build
      val result = successPayment.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a pdf when the cookie exists" in pending

    /*
    //TODO commented out as when running sbt console it will pass all tests the first time but when you run test again ALL controller test complain. It is something to do with the chunked response as the problem does not happen if you call the service directly. I notice that a java icon stays in my Mac dock after the first test run finishes, so something is not closing.
    "return status OK when creation succeeded" in {
      val request = FakeRequest().
        withCookies(vehicleDetailsModel()).
        withCookies(retainModel())
      val result = success.createPdf(request)
      status(result) should equal(OK)
    }*/
  }

  private def build = {
    val emailService = new TestEmailService
    val injector = testInjector(
      new ValidatedAuthorised(),
      emailService
    )
    (injector.getInstance(classOf[SuccessPayment]), emailService.stub)
  }

  private val supplyEmailTrue = "true"
}
