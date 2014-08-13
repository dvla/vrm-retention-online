package utils.helpers

import play.api.templates.{Html, HtmlFormat}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken

object CsrfHelper {

  def hiddenFormField(implicit token: CsrfPreventionToken, config: utils.helpers.Config): Html =
    if (config.isCsrfPreventionEnabled) {
      val csrfTokenName = uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName
      Html( s"""<input type="hidden" name="$csrfTokenName" value="${HtmlFormat.escape(token.value)}"/>""")
    } else Html.empty
}