package pdf

import helpers.UnitSpec
import viewmodels.{RetainModel, VehicleAndKeeperDetailsModel}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import services.fakes.VehicleAndKeeperLookupWebServiceConstants._
import uk.gov.dvla.vehicles.presentation.common.services.DateService

final class PdfServiceSpec extends UnitSpec {

  // See getting started documentation from https://pdfbox.apache.org/cookbook/documentcreation.html

  // See http://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0   for how to do the controller.

  "create" should {

    "return a non-empty output stream" in {
      val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(registrationNumber = RegistrationNumberValid,
        make = VehicleMakeValid,
        model = VehicleModelValid,
        title = None,
        firstName = None,
        lastName = None,
        address = None
      )

      val retainModel = RetainModel(
        certificateNumber = "certificateNumber",
        transactionTimestamp = dateService.today.`dd/MM/yyyy`
      )

      val result = pdfService.create(
        vehicleDetails = vehicleAndKeeperDetailsModel,
        transactionId = TransactionIdValid
      )

      whenReady(result, longTimeout) { r =>
        r should not equal null
        r.length > 0 should equal(true)
      }
    }
  }

  private val dateService = injector.getInstance(classOf[DateService])
  implicit val pdfService = injector.getInstance(classOf[PdfService])
  private val longTimeout = Timeout(Span(10, Seconds))
}