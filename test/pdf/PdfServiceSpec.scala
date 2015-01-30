package pdf

import composition.{TestDateService, WithApplication}
import helpers.UnitSpec
import models.EligibilityModel
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.TransactionIdValid
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid

final class PdfServiceSpec extends UnitSpec {

  // See getting started documentation from https://pdfbox.apache.org/cookbook/documentcreation.html

  // See http://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0   for how to do the controller.

  "create" should {

//    "return a non-empty output stream" in new WithApplication {
//      val eligibilityModel = EligibilityModel(replacementVRM = ReplacementRegistrationNumberValid)
//
//      val result = pdfService.create(
//        eligibilityModel = eligibilityModel,
//        transactionId = TransactionIdValid,
//        name = "stub name",
//        address = None
//      )
//
//      whenReady(result, longTimeout) { r =>
//        r should not equal null
//        r.length > 0 should equal(true)
//      }
//    }
  }

  private def pdfService = testInjector(new TestDateService).getInstance(classOf[PdfService])

  private def longTimeout = Timeout(Span(30, Seconds))
}
