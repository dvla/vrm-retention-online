package utils.helpers

import app.ConfigProperties.getProperty
import play.api.templates.{Html, HtmlFormat}

object CsrfHelper {

  val csrfPrevention = getProperty("csrf.prevention", default = true)

  def hiddenFormField(implicit token: filters.csrf_prevention.CsrfPreventionAction.CsrfPreventionToken): Html =
    if (csrfPrevention) {
      val csrfTokenName = filters.csrf_prevention.CsrfPreventionAction.TokenName
      Html(s"""<input type="hidden" name="$csrfTokenName" value="${HtmlFormat.escape(token.value)}"/>""")
    } else Html("")
}