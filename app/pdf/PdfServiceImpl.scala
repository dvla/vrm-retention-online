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

  private val v948Blank: Option[File] = {
    val file = new File("v948_blank.pdf")
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
    document.addPage(page2)
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

    writeBody1
    writeBody2

    // Make sure that the content stream is closed:
    contentStream.close()
    page
  }

  private def page2(implicit document: PDDocument): PDPage = {
    val page = new PDPage()
    // Start a new content stream which will "hold" the to be created content
    implicit val contentStream = new PDPageContentStream(document, page)


    // Make sure that the content stream is closed:
    contentStream.close()
    page
  }

  private def setFont(implicit contentStream: PDPageContentStream): Unit = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA_BOLD
    contentStream.setFont(font, 12)
  }

  private def writeBody1(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.beginText()
    setFont
    contentStream.moveTextPositionByAmount(400, 10)
    contentStream.drawString("400, 10")
    contentStream.endText()

//    !!!!!!
//
//
//
//    ???????  READ IN THE OLD PDF AND JUST ADD TO IT!!!!!!!
//
//
//
//    !!!!!!
  }

  private def writeBody2(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.beginText()
    setFont
    contentStream.moveTextPositionByAmount(400, 30)
    contentStream.drawString("400, 30")
    contentStream.endText()
  }

  private def watermark(implicit document: PDDocument): PDDocument = {
    // https://stackoverflow.com/questions/8929954/watermarking-with-pdfbox
    // Caution: You should make sure you match the number of pages in both document. Otherwise, you would end up with a
    // document with number of pages matching the one which has least number of pages.
    v948Blank match {
      case Some(file) =>
        // Load document containing just the watermark image.
        val watermarkDoc = PDDocument.load(file)
        val overlay = new Overlay()
        overlay.overlay(document, watermarkDoc)
      case None => document // Watermark file not found so cannot watermark.
    }
  }
}