package email

import play.api.i18n.Messages

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 *
 */

object FailureEmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  def buildWith: Contents = {
    Contents(buildHtml, buildText)
  }

  private def buildHtml: String =
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
     |	<strong>
     |    <ul>
     |      <li>${Messages("email.failure.line1")}</li>
     |     <li>${Messages("email.failure.line2")}</li>
     |   </ul>
     | </strong>
     |</p>
     |
     |<p>
     |<ul>
     |  <li>${Messages("email.failure.line3")}</li>
     |  <li>${Messages("email.failure.line4")}</li>
     |  <li>${Messages("email.failure.line5")}</li>
     |  <li>${Messages("email.failure.line6")}</li>
     |  <li>${Messages("email.failure.line7")}</li>
     |  <li>${Messages("email.failure.line8")}</li>
     |  <li>${Messages("email.failure.line9")}</li>
     |  <li>${Messages("email.failure.line10")}</li>
     |  <li>${Messages("email.failure.line11")}</li>
     |  <li>${Messages("email.failure.line12")}</li>
     |  <li>${Messages("email.failure.line14")}</li>
     |</ul>
     |</p>
  """.stripMargin

  private def buildText: String =
  s"""
      |${Messages("email.failure.line1")}
      |${Messages("email.failure.line2")}
      |${Messages("email.failure.line3")}
      |${Messages("email.failure.line4")}
      |${Messages("email.failure.line5")}
      |${Messages("email.failure.line6")}
      |${Messages("email.failure.line7")}
      |${Messages("email.failure.line8")}
      |${Messages("email.failure.line9")}
      |${Messages("email.failure.line10")}
      |${Messages("email.failure.line11")}
      |${Messages("email.failure.line12")}
      |${Messages("email.failure.line13")}
      |${Messages("email.failure.line14")}
    """.stripMargin
}
