package pdf

import java.io.{ByteArrayOutputStream, File, OutputStream}
import com.google.inject.Inject
import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.RetainModel
import org.apache.pdfbox.Overlay
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.{PDFont, PDType1Font}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import pdf.PdfServiceImpl.{blankPage, v948Blank}
import play.api.Logger
import services.DateService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class PdfServiceImpl @Inject()(dateService: DateService) extends PdfService {

  def create(implicit vehicleDetails: VehicleDetailsModel, retainModel: RetainModel): Future[Array[Byte]] = Future {
    implicit val output = new ByteArrayOutputStream()
    v948
    output.toByteArray
  }

  private def v948(implicit vehicleDetails: VehicleDetailsModel, retainModel: RetainModel, output: OutputStream) = {
    // Create a document and add a page to it
    implicit val document = new PDDocument()

    document.addPage(page1)
    document.addPage(blankPage)
    val documentWatermarked = combineWithOriginal

    // Save the results and ensure that the document is properly closed:
    documentWatermarked.save(output)
    documentWatermarked.close()
    documentWatermarked
  }

  private def page1(implicit vehicleDetails: VehicleDetailsModel, retainModel: RetainModel, document: PDDocument): PDPage = {
    val page = new PDPage()
    // Start a new content stream which will "hold" the to be created content
    implicit val contentStream = new PDPageContentStream(document, page)

    writeVrn(vehicleDetails.registrationNumber)
    writeDateOfRetentionAndTransactionId(retainModel.transactionId)

    // Make sure that the content stream is closed:
    contentStream.close()
    page
  }

  private def setFont(implicit contentStream: PDPageContentStream): Unit = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA_BOLD
    contentStream.setFont(font, 12)
  }

  private def writeVrn(registrationNumber: String)(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.beginText()
    setFont
    contentStream.moveTextPositionByAmount(45, 390)
    contentStream.drawString(registrationNumber)
    contentStream.endText()
  }

  private def writeDateOfRetentionAndTransactionId(transactionId: String)(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.beginText()
    setFont
    contentStream.moveTextPositionByAmount(45, 280)
    contentStream.drawString(s"Date of retention: ${dateService.today.`dd/MM/yyyy`}")
    contentStream.endText()

    contentStream.beginText()
    setFont
    contentStream.moveTextPositionByAmount(45, 260)
    contentStream.drawString(s"Transaction ID: $transactionId")
    contentStream.endText()
  }

  private def combineWithOriginal(implicit document: PDDocument): PDDocument = {
    // https://stackoverflow.com/questions/8929954/watermarking-with-pdfbox
    // Caution: You should make sure you match the number of pages in both document. Otherwise, you would end up with a
    // document with number of pages matching the one which has least number of pages.
    v948Blank match {
      case Some(blankFile) =>
        // Load document containing just the watermark image.
        val blankDoc = PDDocument.load(blankFile)
        val overlay = new Overlay()
        overlay.overlay(document, blankDoc)
      case None => document // Other file was not found so cannot combine with it.
    }
  }
}

object PdfServiceImpl {

  private val v948Blank: Option[File] = {
    val filename = "v948_blank.pdf"
    val file = new File(filename)
    if (file.exists()) Some(file)
    else {
      Logger.error("PdfService could not find blank file for v948")
      None // TODO When we move the service into a micro-service this should throw an error as the micro-service is in a bad state.
    }
  }

  private def blankPage(implicit document: PDDocument): PDPage = {
    val page = new PDPage()
    // Start a new content stream which will "hold" the to be created content
    val contentStream = new PDPageContentStream(document, page)
    // Make sure that the content stream is closed:
    contentStream.close()
    page
  }
}