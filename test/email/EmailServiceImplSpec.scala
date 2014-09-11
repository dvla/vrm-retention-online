package email

import helpers.{WithApplication, UnitSpec}
import viewmodels.{EligibilityModel, RetainModel, VehicleAndKeeperDetailsModel}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import services.fakes.VehicleAndKeeperLookupWebServiceConstants._
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import services.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import services.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid

final class EmailServiceImplSpec extends UnitSpec {

  "sendEmail" should {

    "send an email with an attachment to a business email address" in new WithApplication {

      val vehicleAndKeeperDetails = VehicleAndKeeperDetailsModel(registrationNumber = RegistrationNumberValid,
        make = VehicleMakeValid,
        model = VehicleModelValid,
        title = None,
        firstName = None,
        lastName = None,
        address = None
      )

      val eligibility = EligibilityModel(replacementVRM = ReplacementRegistrationNumberValid)

      val retain = RetainModel(
        certificateNumber = "certificateNumber",
        transactionTimestamp = dateService.today.`dd/MM/yyyy`
      )

//      val result = emailService.sendEmail(
//        emailAddress = TraderBusinessEmailValid,
//        vehicleAndKeeperDetailsModel = vehicleAndKeeperDetails,
//        eligibilityModel = eligibility,
//        retainModel = retain
//      )
//
//      whenReady(result, longTimeout) { r =>
//        r should not equal null
//        r.length > 0 should equal(true)
//      }
    }
  }

  private lazy val dateService = testInjectorOverrideDev().getInstance(classOf[DateService])
  private val longTimeout = Timeout(Span(10, Seconds))
}