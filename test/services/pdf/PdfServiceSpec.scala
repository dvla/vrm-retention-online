package services.pdf

import helpers.UnitSpec
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.{PDFont, PDType1Font}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.scalatest.Ignore

@Ignore
final class PdfServiceSpec extends UnitSpec {

  // See getting started documentation from https://pdfbox.apache.org/cookbook/documentcreation.html

  // See http://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0   for how to do the controller.

  "save" should {
    "create pdf file" in {
      // Create a new empty document
      val document = new PDDocument()
      // Create a new blank page and add it to the document
      val blankPage = new PDPage()
      document.addPage(blankPage)

      // Save the newly created document
      document.save("BlankPage.pdf")

      // finally make sure that the document is properly
      // closed.
      document.close()

      true should equal(true)
    }

    "create pdf file using base font" in {
      // Create a document and add a page to it
      val document = new PDDocument()
      val page = new PDPage()
      document.addPage(page)

      // Create a new font object selecting one of the PDF base fonts
      val font: PDFont = PDType1Font.HELVETICA_BOLD

      // Start a new content stream which will "hold" the to be created content
      val contentStream = new PDPageContentStream(document, page)

      // Define a text content stream using the selected font, moving the cursor and drawing the text "Hello World"
      contentStream.beginText()
      contentStream.setFont(font, 12)
      contentStream.moveTextPositionByAmount(100, 700)
      contentStream.drawString("Hello World")
      contentStream.endText()

      // Make sure that the content stream is closed:
      contentStream.close()

      // Save the results and ensure that the document is properly closed:
      document.save("Hello World.pdf")
      document.close()

      true should equal(true)
    }
  }
}