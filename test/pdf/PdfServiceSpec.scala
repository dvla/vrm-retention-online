package pdf

import helpers.UnitSpec
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.{KeeperDetailsModel, VehicleLookupFormModel}
import services.fakes.FakeAddressLookupService.addressWithUprn
import services.fakes.FakeVehicleLookupWebService._

final class PdfServiceSpec extends UnitSpec {

  // See getting started documentation from https://pdfbox.apache.org/cookbook/documentcreation.html

  // See http://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0   for how to do the controller.

//  "create" should {
//
//    "return a pdf file" in {
//      val pdfService: PdfService = new PdfServiceImpl()
//      val vehicleDetailsModel = VehicleDetailsModel(registrationNumber = RegistrationNumberValid,
//        vehicleMake = VehicleMakeValid,
//        vehicleModel = VehicleModelValid)
//
//      val keeperDetailsModel = KeeperDetailsModel(
//        title = "title",
//        firstName = "firstName",
//        lastName = "lastName",
//        address = addressWithUprn
//      )
//
//      val vehicleLookupFormModel = VehicleLookupFormModel(
//        referenceNumber = ReferenceNumberValid,
//        registrationNumber = RegistrationNumberValid,
//        postcode = KeeperPostcodeValid,
//        keeperConsent = KeeperConsentValid
//      )
//
//      val result = pdfService.create(
//        vehicleDetails = vehicleDetailsModel,
//        keeperDetails = keeperDetailsModel,
//        vehicleLookupFormModel = vehicleLookupFormModel
//      )
//
//      whenReady(result, timeout) { r =>
//        r should not equal null
//      }
//    }
//  }
}