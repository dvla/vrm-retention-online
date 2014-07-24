package pdf

import java.io.{OutputStream, ByteArrayOutputStream}
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.{KeeperDetailsModel, VehicleLookupFormModel}
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.{PDFont, PDType1Font}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PdfServiceImpl() extends PdfService {

  def create(vehicleDetails: VehicleDetailsModel,
             keeperDetails: KeeperDetailsModel,
             vehicleLookupFormModel: VehicleLookupFormModel): Future[PDDocument] = Future {
    implicit val output = new ByteArrayOutputStream()
    v948
  }

  private def v948(implicit output: OutputStream) = {
    // Create a document and add a page to it
    implicit val document = new PDDocument()
    document.addPage(page1)

    // Save the results and ensure that the document is properly closed:
    document.save(output)
    document.close()
    document
  }

  private def page1(implicit document: PDDocument): PDPage = {
    val page = new PDPage()
    // Start a new content stream which will "hold" the to be created content
    implicit val contentStream = new PDPageContentStream(document, page)

    // Define a text content stream using the selected font, moving the cursor and drawing the text "Hello World"
    contentStream.beginText()

    setFont
    writeBody

    contentStream.endText()

    // Make sure that the content stream is closed:
    contentStream.close()
    page
  }

  private def setFont(implicit contentStream: PDPageContentStream) = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA_BOLD
    contentStream.setFont(font, 12)
  }

  private def writeBody(implicit contentStream: PDPageContentStream) = {
    contentStream.moveTextPositionByAmount(100, 700)
    contentStream.drawString("Hello World")
  }
}