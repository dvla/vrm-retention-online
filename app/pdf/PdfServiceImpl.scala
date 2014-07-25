package pdf

import java.io.{ByteArrayOutputStream, File, OutputStream}
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.{KeeperDetailsModel, VehicleLookupFormModel}
import org.apache.pdfbox.Overlay
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.{PDFont, PDType1Font}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PdfServiceImpl() extends PdfService {

  private val watermarkedFile: Option[File] = {
    val file = new File("v948_background.pdf")
    if (file.exists()) Some(file)
    else None
  }

  def create(vehicleDetails: VehicleDetailsModel,
             keeperDetails: KeeperDetailsModel,
             vehicleLookupFormModel: VehicleLookupFormModel): Future[Array[Byte]] = Future {
    implicit val output = new ByteArrayOutputStream()
    v948
    output.toByteArray
  }

  private def v948(implicit output: OutputStream) = {
    // Create a document and add a page to it
    implicit val document = new PDDocument()

    document.addPage(page1)
    val documentWatermarked = watermark

    // Save the results and ensure that the document is properly closed:
    documentWatermarked.save(output)
    documentWatermarked.close()
    documentWatermarked
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

  private def setFont(implicit contentStream: PDPageContentStream): Unit = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA_BOLD
    contentStream.setFont(font, 12)
  }

  private def writeBody(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.moveTextPositionByAmount(100, 700)
    contentStream.drawString("Hello World")
  }

  private def watermark(implicit document: PDDocument): PDDocument = {
    // https://stackoverflow.com/questions/8929954/watermarking-with-pdfbox
    // Caution: You should make sure you match the number of pages in both document. Otherwise, you would end up with a
    // document with number of pages matching the one which has least number of pages.
    watermarkedFile match {
      case Some(file) =>
        // Load document containing just the watermark image.
        val watermarkDoc = PDDocument.load(file)
        val overlay = new Overlay()
        overlay.overlay(document, watermarkDoc)
      case None => document // Watermark file not found so cannot watermark.
    }
  }
}