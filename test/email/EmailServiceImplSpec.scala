package email

import composition.WithApplication
import helpers.UnitSpec
import models._
import org.apache.commons.mail.HtmlEmail
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.TransactionTimestampValid

final class EmailServiceImplSpec extends UnitSpec {

  //  "sendEmail" should {
  //
  //    "send an email with an attachment to a business email address" in new WithApplication {
  //      val vehicleAndKeeperDetails = VehicleAndKeeperDetailsModel(registrationNumber = RegistrationNumberValid,
  //        make = VehicleMakeValid,
  //        model = VehicleModelValid,
  //        title = None,
  //        firstName = None,
  //        lastName = None,
  //        address = None
  //      )
  //      val eligibility = EligibilityModel(replacementVRM = ReplacementRegistrationNumberValid)
  //      val retain = RetainModel(
  //        certificateNumber = "certificateNumber",
  //        transactionTimestamp = dateService.today.`dd/MM/yyyy`
  //      )
  //
  //      //      val result = emailService.sendEmail(
  //      //        emailAddress = TraderBusinessEmailValid,
  //      //        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
  //      //        eligibilityModel = eligibility,
  //      //        retainModel = retain
  //      //      )
  //      //
  //      //      whenReady(result, longTimeout) { r =>
  //      //        r should not equal null
  //      //        r.length > 0 should equal(true)
  //      //      }
  //    }
  //  }

  "htmlMessage" should {

    "return html with business details when user type business" in new WithApplication {
      val htmlEmail = new HtmlEmail()
      val result = emailService.htmlMessage(
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
        eligibilityModel = eligibility,
        retainModel = retain,
        transactionId = transactionId,
        htmlEmail = htmlEmail,
        confirmFormModel = confirmFormModel,
        businessDetailsModel = businessDetailsModel,
        isKeeper = true
      )

      result.toString should include(vehicleAndKeeperDetails.registrationNumber)
      result.toString should include(eligibility.replacementVRM)
      result.toString should include(retain.certificateNumber)
      result.toString should include(retain.transactionTimestamp)
      result.toString should include(transactionId)
      result.toString should include(TraderBusinessNameValid)
      result.toString should include(TraderBusinessEmailValid)
    }

    "return expected without business details html when user type keeper" in new WithApplication {
      val htmlEmail = new HtmlEmail()
      val result = emailService.htmlMessage(
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
        eligibilityModel = eligibility,
        retainModel = retain,
        transactionId = transactionId,
        htmlEmail = htmlEmail,
        confirmFormModel = confirmFormModel,
        businessDetailsModel = None,
        isKeeper = false
      )

      result.toString should include(vehicleAndKeeperDetails.registrationNumber)
      result.toString should include(eligibility.replacementVRM)
      result.toString should include(retain.certificateNumber)
      result.toString should include(retain.transactionTimestamp)
      result.toString should include(transactionId)
      result.toString should not include TraderBusinessNameValid
      result.toString should not include TraderBusinessEmailValid
    }
  }

  private lazy val emailService: EmailService = testInjector().getInstance(classOf[EmailService])
  private val vehicleAndKeeperDetails = VehicleAndKeeperDetailsModel(registrationNumber = RegistrationNumberValid,
    make = VehicleMakeValid,
    model = VehicleModelValid,
    title = None,
    firstName = None,
    lastName = None,
    address = None
  )
  private val eligibility = EligibilityModel(replacementVRM = ReplacementRegistrationNumberValid)
  private val retain = RetainModel(
    certificateNumber = "certificateNumber",
    transactionTimestamp = TransactionTimestampValid
  )
  private val transactionId = "stubTransactionId"
  private val confirmFormModel = Some(ConfirmFormModel(keeperEmail = KeeperEmailValid))
  private val businessDetailsModel = Some(BusinessDetailsModel(name = TraderBusinessNameValid, contact = TraderBusinessContactValid, email = TraderBusinessEmailValid, address = addressWithUprn))
}
