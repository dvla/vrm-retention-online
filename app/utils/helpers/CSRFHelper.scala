package utils.helpers

import play.api.templates.{Html, HtmlFormat}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName

object CsrfHelper {

  def hiddenFormField(implicit token: CsrfPreventionToken, config: utils.helpers.Config): Html =
    if (config.isCsrfPreventionEnabled) {
      Html( s"""<input type="hidden" name="$TokenName" value="${HtmlFormat.escape(token.value)}"/>""")
    } else Html.empty
}