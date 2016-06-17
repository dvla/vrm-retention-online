package email

import play.api.i18n.{Lang, Messages}

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 *
 */

object FailureEmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  def buildWith(implicit lang: Lang): Contents = Contents(buildHtml, buildText)

  private def buildHtml(implicit lang: Lang): String = {
    val pMargin = "style=\"margin: 16px 0 0 32px\""
    s"""
       |<html>
       |<head>
       |</head>
       |<body>
       |
       |<p $pMargin>
       |  <strong>
       |    ${Messages("email.failure.line1")}<br>
       |    ${Messages("email.failure.line2")}
       |  </strong>
       |</p>
       |
       |<p>
       |  <p $pMargin>${Messages("email.failure.line4")}</p>
       |  <p $pMargin>${Messages("email.failure.line6")}</p>
       |  <p $pMargin>${Messages("email.failure.line7")}<br>
       |  ${Messages("email.failure.line8")}<br>
       |  ${Messages("email.failure.line9")}</p>
       |  <p $pMargin>${Messages("email.failure.line11")}<br>
       |  ${Messages("email.failure.line12")}<br>
       |  ${Messages("email.failure.line13")}</p>
       |</p>
    """.stripMargin
  }

  private def buildText(implicit lang: Lang): String =
    s"""
      |${Messages("email.failure.line1")}
      |${Messages("email.failure.line2")}
      |${Messages("email.failure.line4")}
      |${Messages("email.failure.line6")}
      |${Messages("email.failure.line7")}
      |${Messages("email.failure.line8")}
      |${Messages("email.failure.line9")}
      |${Messages("email.failure.line11")}
      |${Messages("email.failure.line12")}
      |${Messages("email.failure.line13")}
    """.stripMargin
}
