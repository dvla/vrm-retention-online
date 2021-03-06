package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.loadBalancerUrl
import composition.webserviceclients.paymentsolve.{RefererFromHeaderBinding, ValidatedCardDetails}
import composition.webserviceclients.vrmretentionretain.{VrmRetentionRetainFailure, TestVrmRetentionRetainWebService}
import controllers.Payment.AuthorisedStatus
import email.{RetainEmailServiceImpl, RetainEmailService}
import helpers.TestWithApplication
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import models.EligibilityModel
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import pages.vrm_retention.MicroServiceErrorPage
import pages.vrm_retention.RetainFailurePage
import pages.vrm_retention.SuccessPage
import pdf.PdfService
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers.{LOCATION, REFERER}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import utils.helpers.Config
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.{BusinessConsentValid, KeeperConsentValid}
import webserviceclients.vrmretentionretain.VRMRetentionRetainRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainWebService

class RetainUnitSpec extends UnitSpec {

  val keeperEmail = "keeper.example@test.com"
  val businessEmail = "business.example@test.com"

  "retain" should {
    "redirect to error page when cookies do not exist" in new TestWithApplication {
      val request = FakeRequest()

      val result = retain.retain(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should
          equal(Some("/error/user%20went%20to%20Retain%20retainMark%20without%20correct%20cookies"))
      }
    }

    "redirect to success page when no fees due and required cookies are present" in new TestWithApplication {
      val result = retain.retain(request())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "redirect to success page when fees due and required cookies are present" in new TestWithApplication {
      val result = retain.retain(request())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
      }
    }

    "redirect to retain failure with a reg number that cannot be retained" in new TestWithApplication {
      val (retentionController, _) = retainControllerAndWebServiceMock((new VrmRetentionRetainFailure).stub)
      val result = retentionController.retain(request())

      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(RetainFailurePage.address))
      }
    }

    "redirect to error page when there are fees due but the payment status is not AUTHORISED" in new TestWithApplication {
      val result = retain.retain(request(paymentStatus = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "not send a payment email to the registered keeper and " +
      "not to the business when registered keeper is chosen and keeper email is not supplied" in new TestWithApplication {
      val (retainController, wsMock) = retainControllerAndWebServiceMock()
      val retentionRetainRequestArg = ArgumentCaptor.forClass(classOf[VRMRetentionRetainRequest])

      // user type: keeper
      // businessDetailsModel is populated
      // confirmModel created with no keeper email supplied
      val result = retainController.retain(request(keeperEmail = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(retentionRetainRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails = retentionRetainRequestArg.getValue.paymentSolveUpdateRequest.businessReceiptEmails
        paymentSuccessReceiptEmails shouldBe empty

        val retentionSuccessEmailRequests = retentionRetainRequestArg.getValue.successEmailRequests
        retentionSuccessEmailRequests shouldBe empty
      }
    }

    "send a payment email to the registered keeper only and " +
      "not to the business when registered keeper is chosen and keeper email is supplied" in new TestWithApplication {
      val (retainController, wsMock) = retainControllerAndWebServiceMock()
      val retentionRetainRequestArg = ArgumentCaptor.forClass(classOf[VRMRetentionRetainRequest])

      // user type: keeper
      // businessDetailsModel is populated
      // confirmModel created with the keeper email supplied
      val result = retainController.retain(request())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(retentionRetainRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails = retentionRetainRequestArg.getValue.paymentSolveUpdateRequest.businessReceiptEmails
        paymentSuccessReceiptEmails.size should equal(1) // Based on user type = keeper
        paymentSuccessReceiptEmails.head.toReceivers should equal(Some(List(keeperEmail)))

        val retentionSuccessEmailRequests = retentionRetainRequestArg.getValue.successEmailRequests
        // Email for the keeper because the keeper email is specified in confirmModel.
        // No business email because the user type is keeper
        retentionSuccessEmailRequests.size should equal(1)

        val successEmail = retentionSuccessEmailRequests.head
        successEmail.toReceivers should equal(Some(List(keeperEmail)))
        successEmail.attachment shouldBe defined
      }
    }

    "send a payment email to the business acting on behalf of the keeper and " +
      "not to the keeper when business is chosen and send retention success emails " +
      "to both business and keeper when keeper email is supplied" in new TestWithApplication {
      val (retainController, wsMock) = retainControllerAndWebServiceMock()
      val retentionRetainRequestArg = ArgumentCaptor.forClass(classOf[VRMRetentionRetainRequest])

      // user type: business
      // businessDetailsModel is populated
      // confirmModel created with the keeper email supplied
      val result = retainController.retain(request(keeperConsent = BusinessConsentValid))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(retentionRetainRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails = retentionRetainRequestArg.getValue.paymentSolveUpdateRequest.businessReceiptEmails
        paymentSuccessReceiptEmails.size should equal(1) // Based on user type = business
        paymentSuccessReceiptEmails.head.toReceivers should equal(Some(List(businessEmail)))

        val retentionSuccessEmailRequests = retentionRetainRequestArg.getValue.successEmailRequests
        // 1 for business because user type = business and
        // 1 for keeper because the keeper email is supplied in the confirmModel
        retentionSuccessEmailRequests.size should equal(2)

        val businessSuccessEmail = retentionSuccessEmailRequests.head
        val keeperSuccessEmail = retentionSuccessEmailRequests(1)

        businessSuccessEmail.toReceivers should equal(Some(List(businessEmail)))
        keeperSuccessEmail.toReceivers should equal(Some(List(keeperEmail)))

        businessSuccessEmail.attachment shouldBe defined
        keeperSuccessEmail.attachment shouldBe None
      }
    }

    "send a payment email and a retention success email to the business acting on behalf of the keeper when " +
      "business is chosen and no keeper email is supplied" in new TestWithApplication {
      val (retainController, wsMock) = retainControllerAndWebServiceMock()
      val retentionRetainRequestArg = ArgumentCaptor.forClass(classOf[VRMRetentionRetainRequest])

      // user type: business
      // businessDetailsModel is populated
      // confirmModel created with no keeper email supplied
      val result = retainController.retain(request(keeperConsent = BusinessConsentValid, keeperEmail = None))
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))

        verify(wsMock).invoke(retentionRetainRequestArg.capture(), any[TrackingId])

        val paymentSuccessReceiptEmails = retentionRetainRequestArg.getValue.paymentSolveUpdateRequest.businessReceiptEmails
        paymentSuccessReceiptEmails.size should equal(1)
        paymentSuccessReceiptEmails.head.toReceivers should equal(Some(List(businessEmail)))

        val retentionSuccessEmailRequests = retentionRetainRequestArg.getValue.successEmailRequests
        // Email for the business because the user type is business.
        // No keeper email because no keeper email supplied in ConfirmModel
        retentionSuccessEmailRequests.size should equal(1)

        val successEmail = retentionSuccessEmailRequests.head
        successEmail.toReceivers should equal(Some(List(businessEmail)))
        successEmail.attachment shouldBe defined
      }
    }
  }

  private def request(referer: String = loadBalancerUrl,
                      paymentStatus: Option[String] = Some(AuthorisedStatus),
                      keeperConsent: String = KeeperConsentValid,
                      keeperEmail: Option[String] = KeeperEmailValid): FakeRequest[AnyContentAsEmpty.type] = {
    val refererHeader = (REFERER, Seq(referer))
    val headers = FakeHeaders(data = Seq(refererHeader))
    FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
      withCookies(
        vehicleAndKeeperLookupFormModel(registrationNumber = "A1", keeperConsent = keeperConsent),
        businessDetailsModel(),
        transactionId(),
        paymentModel(paymentStatus = paymentStatus),
        paymentTransNo(),
        vehicleAndKeeperDetailsModel(registrationNumber = "A1"),
        confirmFormModel(keeperEmail = keeperEmail),
        eligibilityModel()
      )
  }

  // This method returns a retain controller with all of its dependencies mocked
  // and no references to those mocks
  private def retain: Retain = testInjector(
    new ValidatedCardDetails(),
    new RefererFromHeaderBinding
  ).getInstance(classOf[Retain])

  // This method returns a retain controller as well as a reference to the mock VRMRetentionRetainWebService that
  // ultimately makes the network call to the vrm-retention-retain web service.
  private def retainControllerAndWebServiceMock(retainWsMock: VRMRetentionRetainWebService =
                                                (new TestVrmRetentionRetainWebService).stub)
                                                : (Retain, VRMRetentionRetainWebService) = {
    val retain = testInjector(
      new ValidatedCardDetails(),
      new RefererFromHeaderBinding,
      // Bind the mock to the trait. We have a reference to the mock which we can pass out of this method
      // so client code can perform expectations on it
      new ScalaModule() {
        override def configure(): Unit = bind[VRMRetentionRetainWebService].toInstance(retainWsMock)
      },
      // By default the testInjector mocks the RetainEmailService. However, we want a real instance of the
      // RetainEmailService because it contains logic that needs to be tested. So we bind a real instance
      // that has mocks for all its dependencies
      new ScalaModule() {
        override def configure(): Unit = bind[RetainEmailService].toInstance(retainEmailServiceInstance)
      }
    ).getInstance(classOf[Retain])
    (retain, retainWsMock)
  }

  private def retainEmailServiceInstance: RetainEmailService = {
    val emailServiceMock = mock[EmailService]

    val pdfServiceMock = mock[PdfService]
    when(pdfServiceMock.create(
      any[EligibilityModel],
      any[String],
      any[String],
      any[Option[AddressModel]],
      any[TrackingId])
    )
    .thenReturn(Array.ofDim[Byte](0))

    val configMock = mock[Config]
    when(configMock.emailWhitelist).thenReturn(Some(List("@test.com")))
    when(configMock.purchaseAmountInPence).thenReturn("8000")

    new RetainEmailServiceImpl(emailServiceMock, pdfServiceMock, configMock)
  }
}
