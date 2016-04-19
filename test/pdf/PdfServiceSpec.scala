package pdf

import helpers.WithApplication
import helpers.UnitSpec
import models.EligibilityModel
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.TransactionIdValid
import webserviceclients.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid

class PdfServiceSpec extends UnitSpec {

  // See getting started documentation from https://pdfbox.apache.org/1.8/cookbook/documentcreation.html

  // See http://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0
  // for how to do the controller.

  "create" should {
    "return a non-empty output stream" in new WithApplication {
      val eligibilityModel = EligibilityModel(replacementVRM = ReplacementRegistrationNumberValid)

      val pdf = pdfService.create(
        eligibilityModel = eligibilityModel,
        transactionId = TransactionIdValid,
        name = "stub name",
        address = None,
        TrackingId("")
      )

      pdf should not equal null
      pdf.length > 0 should equal(true)
    }
  }

  private def pdfService = testInjector().getInstance(classOf[PdfService])

  private def longTimeout = Timeout(Span(30, Seconds))
}