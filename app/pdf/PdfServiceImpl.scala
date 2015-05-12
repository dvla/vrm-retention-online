package pdf

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

import com.google.inject.Inject
import models.EligibilityModel
import org.apache.pdfbox.Overlay
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.preflight.PreflightDocument
import org.apache.pdfbox.preflight.exception.SyntaxValidationException
import org.apache.pdfbox.preflight.parser.PreflightParser
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import pdf.PdfServiceImpl.blankPage
import pdf.PdfServiceImpl.fontDefaultSize
import pdf.PdfServiceImpl.v948Blank
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear

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
      case e: Exception => Logger.error(s"PdfServiceImpl v948 error when combining and saving: ${e.getStackTrace}")
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
      case e: Exception => Logger.error(s"PdfServiceImpl v948 page1 error when writing vrn and dateOfRetention: ${e.getStackTrace}")
    } finally {
      // Make sure that the content stream is closed:
      contentStream.close()
    }
    page
  }

  private def fontHelvetica(size: Int)(implicit contentStream: PDPageContentStream): PDFont = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA
    contentStream.setFont(font, size)
    font
  }

  private def fontHelveticaBold(size: Int)(implicit contentStream: PDPageContentStream): PDFont = {
    // Create a new font object selecting one of the PDF base fonts
    val font: PDFont = PDType1Font.HELVETICA_BOLD
    contentStream.setFont(font, size)
    font
  }

  private def width(font: PDFont, content: String, fontSize: Int) = {
    // Return the width of a bounding box that surrounds the string.
    font.getStringWidth(content) / 1000 * fontSize
  }

  private def wrapText(words: List[String]): List[List[String]] = words match {
    case Nil => Nil
    case _ =>
      val output = (words.inits.dropWhile { _.mkString(" ").length > 30 }) next;
      output :: wrapText(words.drop(output.length))
  }

  private def writeCustomerNameAndAddress(name: String, address: Option[AddressModel])(implicit contentStream: PDPageContentStream): Unit = {

    var positionY = 580

    wrapText(name split(" ") toList) foreach {
      words => {
        contentStream.beginText()
        fontHelvetica(fontDefaultSize)
        contentStream.moveTextPositionByAmount(330, positionY)
        contentStream.drawString(words.mkString(" "))
        contentStream.endText()
        positionY = positionY - 15
      }
    }

    address.map { a =>
      for (line <- a.address) {
        contentStream.beginText()
        fontHelvetica(fontDefaultSize)
        contentStream.moveTextPositionByAmount(330, positionY)
        contentStream.drawString(line)
        contentStream.endText()
        positionY = positionY - 15
      }
    }
  }

  private def writeVrn(registrationNumber: String)(implicit contentStream: PDPageContentStream, document: PDDocument): Unit = {
    contentStream.beginText()
    val size = 26
    val font = fontHelveticaBold(size = size)
    contentStream.moveTextPositionByAmount(45, 385)
    contentStream.moveTextPositionByAmount((180 - width(font, registrationNumber, fontSize = size)) / 2, 0) // Centre the text.
    contentStream.drawString(registrationNumber)
    contentStream.endText()
  }

  private def writeTransactionId(transactionId: String)(implicit contentStream: PDPageContentStream, document: PDDocument): Unit = {
    contentStream.beginText()
    val size = 18
    val font = fontHelveticaBold(size = 18)
    contentStream.moveTextPositionByAmount(340, 390)
    contentStream.moveTextPositionByAmount((200 - width(font, transactionId, fontSize = size)) / 2, 0) // Centre the text.
    contentStream.drawString(transactionId) // Transaction ID
    contentStream.endText()
  }

  private def writeDateOfRetention()(implicit contentStream: PDPageContentStream): Unit = {
    val today = DayMonthYear.from(new DateTime(dateService.now, DateTimeZone.forID("Europe/London")))
    val dateStamp = today.`dd shortMonth yyyy`
    val timeStamp = today.`HH:mm`
    val font = fontHelvetica(size = fontDefaultSize)

    contentStream.beginText()
    contentStream.moveTextPositionByAmount(50, 280)
    contentStream.moveTextPositionByAmount((110 - width(font, dateStamp, fontDefaultSize)) / 2, 0) // Centre the text.
    contentStream.drawString("DVLA")
    contentStream.endText()

    contentStream.beginText()
    contentStream.moveTextPositionByAmount(45, 260)
    contentStream.moveTextPositionByAmount((85 - width(font, dateStamp, fontDefaultSize)) / 2, 0) // Centre the text.
    contentStream.drawString(dateStamp) // Date of retention
    contentStream.endText()

    contentStream.beginText()
    contentStream.moveTextPositionByAmount(50, 240)
    contentStream.moveTextPositionByAmount((110 - width(font, dateStamp, fontDefaultSize)) / 2, 0) // Centre the text.
    contentStream.drawString(timeStamp) // Time of retention
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

  private val fontDefaultSize = 12

  private val v948Blank: Option[File] = {
    val filename = "vrm-retention-online-v948-blank.pdf"
    val file = new File(filename)
    if (file.exists()) {
      //`PDF/A validation`(file, "v948Blank") // Validate that the file we have loaded meets the specification, otherwise we are writing on top of existing problems.
      Some(file)
    }
    else {
      Logger.error("PdfService could not find blank file for v948")
      None // If we move the service into a micro-service this should throw an error as the micro-service is in a bad state.
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
      case e: Exception => Logger.error(s"PdfServiceImpl v948 page1 error when writing vrn and dateOfRetention: ${e.getStackTrace}")
    } finally {
      contentStream.close() // Make sure that the content stream is closed.
    }
    page
  }
}