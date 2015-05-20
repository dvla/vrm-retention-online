package email

import java.text.SimpleDateFormat

import org.joda.time.{DateTimeZone, Instant}

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
       |	<strong>THIS IS AN AUTOMATED EMAIL - PLEASE DO NOT REPLY.</strong>
       |</p>
       |
       |<p>Payment received.</p>
       |
       |<ul>
       |	<li><strong>£$amountCharged</strong> DVLA Online Assignment of <strong>$assignVrn</strong></li>
       |	<li>Paid by Credit/Debit Card</li>
       |	<li>Date:  <strong>$dateStr</strong></li>
       |	<li>Transaction Number:  <strong>$transactionId</strong></li>
       |</ul>
       |
       |$business
       |
       |<p><i>DVLA, Swansea, SA6 7JL</i></p>
      """.stripMargin

  private def buildText(assignVrn: String,
                        amountCharged: String,
                        transactionId: String,
                        dateStr: String,
                        business: String): String =
    s"""
       |THIS IS AN AUTOMATED EMAIL - PLEASE DO NOT REPLY.
       |
       |
       |Payment received
       |
       |£$amountCharged DVLA Online Retention of $assignVrn
       |
       |Paid by Credit/Debit Card
       |
       |Date:  $dateStr
       |
       |Transaction Number:  $transactionId
       |
       |$business
       |
       |DVLA, Swansea, SA6 7JL
       |
      """.stripMargin

}
