package email

import com.tzavellas.sse.guice.ScalaModule
import composition.{TestConfig, TestDateService}
import helpers.{UnitSpec, WithApplication}
import models.{EligibilityModel, RetainModel, VehicleAndKeeperDetailsModel}
import org.apache.commons.mail.HtmlEmail
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{NoCookieFlags, CookieFlags}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
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

    "return expected html" in new WithApplication {
      val htmlEmail = new HtmlEmail()
      val result = emailService.htmlMessage(
        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
        eligibilityModel = eligibility,
        retainModel = retain,
        transactionId = transactionId,
        htmlEmail = htmlEmail
      )

      result.toString should include(vehicleAndKeeperDetails.registrationNumber)
      result.toString should include(eligibility.replacementVRM)
      result.toString should include(retain.certificateNumber)
      result.toString should include(retain.transactionTimestamp)
      result.toString should include(transactionId)
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
}
