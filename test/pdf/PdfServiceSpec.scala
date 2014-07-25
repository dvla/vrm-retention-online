package pdf

import helpers.UnitSpec
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.{RetainModel, KeeperDetailsModel, VehicleLookupFormModel}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import services.fakes.FakeAddressLookupService.addressWithUprn
import services.fakes.FakeDateServiceImpl
import services.fakes.FakeDisposeWebServiceImpl.TransactionIdValid
import services.fakes.FakeVehicleLookupWebService._

final class PdfServiceSpec extends UnitSpec {

  // See getting started documentation from https://pdfbox.apache.org/cookbook/documentcreation.html

  // See http://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0   for how to do the controller.

  "create" should {

    "return a non-empty output stream" in {
      val vehicleDetailsModel = VehicleDetailsModel(registrationNumber = RegistrationNumberValid,
        vehicleMake = VehicleMakeValid,
        vehicleModel = VehicleModelValid)

      val retainModel = RetainModel(
        certificateNumber = "certificateNumber",
        transactionId = TransactionIdValid,
        transactionTimestamp = dateService.today.`dd/MM/yyyy`
      )

      val result = pdfService.create(
        vehicleDetails = vehicleDetailsModel,
        retainModel = retainModel
      )

      whenReady(result, longTimeout) { r =>
        r should not equal null
        r.length > 0 should equal(true)
      }
    }
  }

  private val dateService = new FakeDateServiceImpl
  implicit val pdfService = injector.getInstance(classOf[PdfService])
  private val longTimeout = Timeout(Span(10, Seconds))
}