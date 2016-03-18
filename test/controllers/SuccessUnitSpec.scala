package controllers

import composition.{TestEmailService, TestReceiptEmailService}
import composition.webserviceclients.paymentsolve.ValidatedAuthorised
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.retainModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import helpers.WithApplication
import models.{BusinessDetailsModel, ConfirmFormModel, EligibilityModel}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import pages.vrm_retention.LeaveFeedbackPage
import play.api.test.FakeRequest
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.CONTENT_DISPOSITION
import play.api.test.Helpers.CONTENT_TYPE
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.LOCATION
import play.api.test.Helpers.OK
import play.api.test.Helpers.status
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.BusinessConsentValid
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid

class SuccessUnitSpec extends UnitSpec {

  "present" should {
    "display the page when BusinessDetailsModel cookie exists" in new WithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          businessDetailsModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentModel()
        )
      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }

    "display the page when BusinessDetailsModel cookie does not exists" in new WithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentModel()
        )
      val (success, _) = build
      val result = success.present(request)
      status(result) should equal(OK)
    }
  }

  "call the email service when businessDetails cookie exists" in new WithApplication {
    val isKeeper = false
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
        setupBusinessDetails(),
        businessDetailsModel(),
        vehicleAndKeeperDetailsModel(),
        eligibilityModel(),
        confirmFormModel(keeperEmail = None),
        retainModel(),
        transactionId(),
        paymentModel())
    val (success, emailService) = build
    val result = success.present(request)
    whenReady(result) { r =>
      verify(emailService, never).sendEmail(
        any[String],
        any[VehicleAndKeeperDetailsModel],
        any[EligibilityModel],
        any[String],
        any[String],
        any[String],
        any[Option[ConfirmFormModel]],
        any[Option[BusinessDetailsModel]],
        Matchers.eq(isKeeper),
        any[TrackingId]
      )
      verifyNoMoreInteractions(emailService)
    }
  }

  "call the email service when keeper selected to supply an email address and did supply an email" in new WithApplication {
    val isKeeper = true
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel(),
        eligibilityModel(),
        confirmFormModel(keeperEmail = KeeperEmailValid),
        retainModel(),
        transactionId()
      )
    val (success, emailService) = build
    val result = success.present(request)
    whenReady(result) { r =>
      verify(emailService, never).sendEmail(
        any[String],
        any[VehicleAndKeeperDetailsModel],
        any[EligibilityModel],
        any[String],
        any[String],
        any[String],
        any[Option[ConfirmFormModel]],
        any[Option[BusinessDetailsModel]],
        Matchers.eq(isKeeper),
        any[TrackingId]
      )
      verifyNoMoreInteractions(emailService)
    }
  }

  "not call the email service when businessDetails does not cookie" in new WithApplication {
    val isKeeper = false
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
        vehicleAndKeeperDetailsModel(),
        eligibilityModel(),
        confirmFormModel(keeperEmail = None),
        retainModel(),
        transactionId()
      )
    val (success, emailService) = build
    val result = success.present(request)
    whenReady(result) { r =>
      verify(emailService, never).sendEmail(
        any[String],
        any[VehicleAndKeeperDetailsModel],
        any[EligibilityModel],
        any[String],
        any[String],
        any[String],
        any[Option[ConfirmFormModel]],
        any[Option[BusinessDetailsModel]],
        Matchers.eq(isKeeper),
        any[TrackingId]
      )
      verifyNoMoreInteractions(emailService)
    }
  }

  "not call the email service when keeper did not select to supply an email address" in new WithApplication {
    val isKeeper = true
    val request = FakeRequest()
      .withCookies(
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel(),
        eligibilityModel(),
        confirmFormModel(keeperEmail = None),
        retainModel(),
        transactionId()
      )
    val (success, emailService) = build
    val result = success.present(request)
    whenReady(result) { r =>
      verify(emailService, never).sendEmail(
        any[String],
        any[VehicleAndKeeperDetailsModel],
        any[EligibilityModel],
        any[String],
        any[String],
        any[String],
        any[Option[ConfirmFormModel]],
        any[Option[BusinessDetailsModel]],
        Matchers.eq(isKeeper),
        any[TrackingId]
      )
    }
  }

  "finish" should {
    "redirect to LeaveFeedbackPage" in new WithApplication {
      val (success, _) = build
      val result = success.finish(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  "create pdf" should {
    "return a bad request if cookie for EligibilityModel does no exist" in new WithApplication {
      val request = FakeRequest().withCookies(transactionId())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a bad request if cookie for TransactionId does no exist" in new WithApplication {
      val request = FakeRequest().withCookies(eligibilityModel())
      val (success, _) = build
      val result = success.createPdf(request)
      status(result) should equal(BAD_REQUEST)
    }

    "return a pdf when the cookie exists" in new WithApplication {
      val request = FakeRequest()
        .withCookies(vehicleAndKeeperLookupFormModel(),
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel(),
          eligibilityModel(),
          businessDetailsModel(),
          confirmFormModel(),
          retainModel(),
          transactionId(),
          paymentModel()
        )
      val (success, _) = build
      val result = success.createPdf(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
        r.header.headers.get(CONTENT_DISPOSITION) should
          equal(Some(s"attachment;filename=${ReplacementRegistrationNumberValid}-eV948.pdf"))
        r.header.headers.get(CONTENT_TYPE) should equal(Some("application/pdf"))
      }
    }
  }

  private def build = {
    val emailService = new TestEmailService
    val emailReceiptService = new TestReceiptEmailService

     val injector = testInjector(
       new ValidatedAuthorised(),
       emailService,
       emailReceiptService
     )

    (injector.getInstance(classOf[Success]), emailService.stub)
   }
}
