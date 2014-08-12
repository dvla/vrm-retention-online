package utils.helpers

import app.ConfigProperties.getProperty
import play.api.templates.{Html, HtmlFormat}

object CsrfHelper {

  val csrfPrevention = getProperty("csrf.prevention", default = true)

  def hiddenFormField(implicit token: uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken): Html =
    if (csrfPrevention) {
      val csrfTokenName = uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName
      Html(s"""<input type="hidden" name="$csrfTokenName" value="${HtmlFormat.escape(token.value)}"/>""")
    } else Html("")
}