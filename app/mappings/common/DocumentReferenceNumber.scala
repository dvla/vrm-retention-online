package mappings.common

import uk.gov.dvla.vehicles.presentation.common.views.constraints.NumberOnly
import play.api.data.Forms.nonEmptyText
import play.api.data.Mapping

object DocumentReferenceNumber {
  final val MinLength = 11
  final val MaxLength = 11
  final val Pattern = s"\\d{$MinLength,$MaxLength}" // Digits only with specified size.

  def referenceNumber: Mapping[String] = nonEmptyText(MinLength, MaxLength) verifying NumberOnly.rules
}