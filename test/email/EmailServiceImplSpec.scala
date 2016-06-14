package email

import composition.RetainEmailServiceBinding

import helpers.UnitSpec
import helpers.TestWithApplication

import models.BusinessDetailsModel
import models.ConfirmFormModel
import models.EligibilityModel
import models.RetainModel
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import webserviceclients.fakes.AddressLookupServiceConstants.addressWithoutUprn
import webserviceclients.fakes.AddressLookupServiceConstants.KeeperEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.CertificateNumberValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.TransactionTimestampValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleMakeValid
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.VehicleModelValid

final class EmailServiceImplSpec extends UnitSpec {

  "EmailRequest" should {
      "have an attachment if send pdf is true" in new TestWithApplication {
        val (dateService, emailService) = build

        val emailRequest = emailService.emailRequest(
          emailAddress = TraderBusinessEmailValid,
          vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
          eligibilityModel = eligibility,
          certificateNumber = CertificateNumberValid,
          transactionTimestamp = TransactionTimestampValid,
          transactionId = TransactionId,
          confirmFormModel = Some(confirmFormModel),
          businessDetailsModel = Some(businessDetailsModel),
          sendPdf = true,
          isKeeper = false,
          trackingId = TrackingId("123")
        )

        emailRequest shouldBe defined
        emailRequest.get.attachment shouldBe defined
      }

      "not have an attachment if send pdf is false" in new TestWithApplication {
        val (dateService, emailService) = build

        val emailRequest = emailService.emailRequest(
          emailAddress = KeeperEmailValid.get,
          vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
          eligibilityModel = eligibility,
          certificateNumber = CertificateNumberValid,
          transactionTimestamp = TransactionTimestampValid,
          transactionId = TransactionId,
          confirmFormModel = Some(confirmFormModel),
          businessDetailsModel = None,
          sendPdf = false,
          isKeeper = true,
          trackingId = TrackingId("123")
        )

        emailRequest shouldBe defined
        emailRequest.get.attachment shouldBe empty
      }
    }

  "htmlMessage" should {
    "return html with business details when user type is business" in new TestWithApplication {
      val (_, emailService) = build

      val result = emailService.htmlMessage(
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
        eligibilityModel = eligibility,
        certificateNumber = CertificateNumberValid,
        transactionTimestamp = TransactionTimestampValid,
        transactionId = TransactionId,
        confirmFormModel = Some(confirmFormModel),
        businessDetailsModel = Some(businessDetailsModel),
        sendPdf = true,
        isKeeper = false
      )

      val message = result.toString

      message should include(vehicleAndKeeperDetails.registrationNumber)
      message should include(eligibility.replacementVRM)
      message should include(retain.certificateNumber)
      message should include(retain.transactionTimestamp)
      message should include(TransactionId)
      message should include(TraderBusinessNameValid)
      message should include(TraderBusinessEmailValid)
      message should include("V948")
    }

    "return html without business details html when user type is keeper" in new TestWithApplication {
      val (_, emailService) = build

      val result = emailService.htmlMessage(
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
        eligibilityModel = eligibility,
        certificateNumber = CertificateNumberValid,
        transactionTimestamp = TransactionTimestampValid,
        transactionId = TransactionId,
        confirmFormModel = Some(confirmFormModel),
        businessDetailsModel = None,
        sendPdf = false,
        isKeeper = true
      )

      val message = result.toString

      message should include(vehicleAndKeeperDetails.registrationNumber)
      message should include(eligibility.replacementVRM)
      message should include(retain.certificateNumber)
      message should include(retain.transactionTimestamp)
      message should include(TransactionId)
      message shouldNot include(TraderBusinessNameValid)
      message shouldNot include(TraderBusinessEmailValid)
      message shouldNot include("V948")
    }
  }

  private def build = {
    val injector = testInjector(
      new RetainEmailServiceBinding
    )
    (injector.getInstance(classOf[DateService]), injector.getInstance(classOf[RetainEmailService]))
  }

  private def vehicleAndKeeperDetails = VehicleAndKeeperDetailsModel(
    registrationNumber = RegistrationNumberValid,
    make = VehicleMakeValid,
    model = VehicleModelValid,
    title = None,
    firstName = None,
    lastName = None,
    address = None,
    disposeFlag = None,
    keeperEndDate = None,
    keeperChangeDate = None,
    suppressedV5Flag = None
  )

  private def eligibility = EligibilityModel(replacementVRM = ReplacementRegistrationNumberValid)

  private def retain = RetainModel(
    certificateNumber = CertificateNumberValid,
    transactionTimestamp = TransactionTimestampValid
  )

  private val TransactionId = "stubTransactionId"

  private def confirmFormModel = ConfirmFormModel(keeperEmail = KeeperEmailValid)

  private def businessDetailsModel = BusinessDetailsModel(
    name = TraderBusinessNameValid,
    contact = TraderBusinessContactValid,
    email = TraderBusinessEmailValid,
    address = addressWithoutUprn
  )
}
