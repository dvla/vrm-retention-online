package email

import java.text.SimpleDateFormat

import org.joda.time.{DateTimeZone, Instant}
import play.api.i18n.Messages

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 *
 */
object ReceiptEmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  case class BusinessDetails(name: String, contact: String, address: Seq[String])

  def buildWith(assignVrn: String, amountCharged: String, transactionId: String,
                business: Option[BusinessDetails]): Contents = {

    val now = Instant.now.toDateTime(DateTimeZone.forID("Europe/London"))
    val dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(now.toDate)

    Contents(
      buildHtml(assignVrn, amountCharged, transactionId, dateStr, business.map(buildBusinessHtml).getOrElse("")),
      buildText(assignVrn, amountCharged, transactionId, dateStr, business.map(buildBusinessPlain).getOrElse(""))
    )
  }

   private def buildBusinessHtml(business: BusinessDetails): String =
   s"""
      |<ul>
      |<li>Business Name: <strong>${business.name}</strong></li>
      |<li>Business Contact: <strong>${business.contact}</strong></li>
      |<li>Business Address: ${ (for {
           addr <- business.address
         } yield s"<li>$addr</li>").mkString("<ul>", "", "</ul>")  }</li>
       |</ul>
     """.stripMargin

  private def buildBusinessPlain(business: BusinessDetails): String =
  s"""
     |Business Name
   }: ${business.name}
     |Business Contact: ${business.contact}
     |Business Address: ${business.address}
   """.stripMargin

  private def buildHtml(assignVrn: String,
                        amountCharged: String,
                        transactionId: String,
                        dateStr: String,
                        business: String): String =
    s"""
       |<html>
       |<head>
       |</head>
       |<style>
       |p {
       |  line-height: 200%;
       |}
       |ul { list-style: none; padding: 0; margin:0 0 32px 0;}
       |li { margin-bottom: 8px}
       |li > ul {
       |	margin: 16px 0 0 16px;
       |}
       |</style>
       |</head>
       |<body>
       |
       |<p>
       |	<strong>${Messages("email.template.line1")}</strong>
       |</p>
       |
       |<p>${Messages("email.template.line2")}</p>
       |
       |<ul>
       |	<li><strong>&pound;$amountCharged</strong> ${Messages("email.template.line3")} <strong>$assignVrn</strong></li>
       |	<li>${Messages("email.template.line4")}</li>
       |	<li>${Messages("email.template.line5")}  <strong>$dateStr</strong></li>
       |	<li>${Messages("email.template.line6")}  <strong>$transactionId</strong></li>
       |</ul>
       |
       |$business
       |
       |<p><i>${Messages("email.template.line7")}</i></p>
      """.stripMargin

  private def buildText(assignVrn: String,
                        amountCharged: String,
                        transactionId: String,
                        dateStr: String,
                        business: String): String =
    s"""
       |${Messages("email.template.line1")}
       |
       |
       |${Messages("email.template.line2")}
       |
       |£$amountCharged ${Messages("email.template.line3")} $assignVrn
       |
       |${Messages("email.template.line4")}
       |
       |${Messages("email.template.line5")}  $dateStr
       |
       |${Messages("email.template.line6")}  $transactionId
       |
       |$business
       |
       |${Messages("email.template.line7")}
       |
      """.stripMargin

}
