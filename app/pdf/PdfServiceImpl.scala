package pdf

import java.io.{ByteArrayOutputStream, File, OutputStream}
import com.google.inject.Inject
import models.EligibilityModel
import org.apache.pdfbox.Overlay
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.{PDFont, PDType1Font}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.preflight.PreflightDocument
import org.apache.pdfbox.preflight.exception.SyntaxValidationException
import org.apache.pdfbox.preflight.parser.PreflightParser
import pdf.PdfServiceImpl.{blankPage, fontSize, v948Blank}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class PdfServiceImpl @Inject()(dateService: DateService) extends PdfService {

  def create(eligibilityModel: EligibilityModel, transactionId: String, name: String, address: Option[AddressModel]): Future[Array[Byte]] = Future {
    val output = new ByteArrayOutputStream()
    v948(eligibilityModel, transactionId, name, address, output)
    output.toByteArray
  }

  private def v948(eligibilityModel: EligibilityModel, transactionId: String, name: String, address: Option[AddressModel], output: OutputStream) = {
    // Create a document and add a page to it
    implicit val document = new PDDocument()

    document.addPage(page1(eligibilityModel, transactionId, name, address, document))
    document.addPage(blankPage)
    var documentWatermarked: PDDocument = null
    try {
      documentWatermarked = combineWithOriginal
      // Save the results and ensure that the document is properly closed:
      documentWatermarked.save(output)
    } catch {
      case e: Exception => Logger.error(s"PdfServiceImpl v948 error when combining and saving: ${e.getStackTraceString}") // TODO do we need to anonymise this stacktrace?
    } finally {
      documentWatermarked.close()
    }
    documentWatermarked
  }

  private def page1(implicit eligibilityModel: EligibilityModel, transactionId: String, name: String, address: Option[AddressModel], document: PDDocument): PDPage = {
    val page = new PDPage()
    implicit var contentStream: PDPageContentStream = null
    try {
      contentStream = new PDPageContentStream(document, page) // Start a new content stream which will "hold" the to be created content

      writeCustomerNameAndAddress(name, address)
      writeVrn(eligibilityModel.replacementVRM)
      writeTransactionId(transactionId)
      writeDateOfRetention()
    } catch {
      case e: Exception => Logger.error(s"PdfServiceImpl v948 page1 error when writing vrn and dateOfRetention: ${e.getStackTraceString}") // TODO do we need to anonymise this stacktrace?
    } finally {
      // Make sure that the content stream is closed:
      contentStream.close()
    }
    page
  }

  private def setFont(implicit contentStream: PDPageContentStream): PDFont = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA_BOLD
    contentStream.setFont(font, fontSize)
    font
  }

  private def width(font: PDFont, content: String) = {
    // Return the width of a bounding box that surrounds the string.
    font.getStringWidth(content) / 1000 * fontSize
  }

  private def writeCustomerNameAndAddress(name: String, address: Option[AddressModel])(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.beginText()
    setFont
    contentStream.moveTextPositionByAmount(330, 580)
    contentStream.drawString(name)
    contentStream.endText()

    address.map { a =>
      var positionY = 565
      for (line <- a.address.init) {
        contentStream.beginText()
        setFont
        contentStream.moveTextPositionByAmount(330, positionY)
        contentStream.drawString(line)
        contentStream.endText()
        positionY = positionY - 15
      }
    }
  }

  private def writeVrn(registrationNumber: String)(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.beginText()
    val font = setFont
    contentStream.moveTextPositionByAmount(45, 390)
    contentStream.moveTextPositionByAmount((200 - width(font, registrationNumber)) / 2, 0) // Centre the text.
    contentStream.drawString(registrationNumber)
    contentStream.endText()
  }

  private def writeTransactionId(transactionId: String)(implicit contentStream: PDPageContentStream): Unit = {
    contentStream.beginText()
    val font = setFont
    contentStream.moveTextPositionByAmount(330, 390)
    contentStream.moveTextPositionByAmount((200 - width(font, transactionId)) / 2, 0) // Centre the text.
    contentStream.drawString(s"$transactionId") // Transaction ID:
    contentStream.endText()
  }

  private def writeDateOfRetention()(implicit contentStream: PDPageContentStream): Unit = {
    val dateStamp = dateService.today.`dd/MM/yyyy`
    contentStream.beginText()
    val font = setFont
    contentStream.moveTextPositionByAmount(45, 280)
    contentStream.moveTextPositionByAmount((75 - width(font, dateStamp)) / 2, 0) // Centre the text.
    contentStream.drawString(dateStamp) // Date of retention
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

  private val fontSize = 12

  private val v948Blank: Option[File] = {
    val filename = "vrm-retention-online-v948-blank.pdf"
    val file = new File(filename)
    if (file.exists()) {
      `PDF/A validation`(file, "v948Blank") // Validate that the file we have loaded meets the specification, otherwise we are writing on top of existing problems.
      Some(file)
    }
    else {
      Logger.error("PdfService could not find blank file for v948")
      None // TODO When we move the service into a micro-service this should throw an error as the micro-service is in a bad state.
    }
  }

  private def `PDF/A validation`(file: File, docName: String): Unit = {
    val parser = new PreflightParser(file)
    var document: PreflightDocument = null
    try {
      /* Parse the PDF file with PreflightParser that inherits from the NonSequentialParser.
       * Some additional controls are present to check a set of PDF/A requirements.
       * (Stream length consistency, EOL after some Keyword...)
       */
      parser.parse()

      /* Once the syntax validation is done,
       * the parser can provide a PreflightDocument
       * (that inherits from PDDocument)
       * This document process the end of PDF/A validation.
       */
      document = parser.getPreflightDocument
      document.validate()

      // Get validation result
      val result = document.getResult


      // display validation result
      if (!result.isValid) {
        val errors = result.getErrorsList.toList.
          map(error => s"PDF/A error code ${error.getErrorCode}, error details: ${error.getDetails}").
          mkString(", ")
        Logger.warn(s"Document '$docName' does not meet the PDF/A standard because of the following errors - $errors")
      }
    } catch {
      case e: SyntaxValidationException =>
        /* the parse method can throw a SyntaxValidationException
         *if the PDF file can't be parsed.
         * In this case, the exception contains an instance of ValidationResult
         */
        Logger.error(s"PDF/A validation SyntaxValidationException: ${e.getResult}")
    } finally {
      document.close() // Make sure that the document is closed.
    }
  }

  private def blankPage(implicit document: PDDocument): PDPage = {
    val page = new PDPage()
    // Start a new content stream which will "hold" the to be created content
    var contentStream: PDPageContentStream = null
    try {
      contentStream = new PDPageContentStream(document, page)
    } catch {
      case e: Exception => Logger.error(s"PdfServiceImpl v948 page1 error when writing vrn and dateOfRetention: ${e.getStackTraceString}") // TODO do we need to anonymise this stacktrace?
    } finally {
      contentStream.close() // Make sure that the content stream is closed.
    }
    page
  }
}