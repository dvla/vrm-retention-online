package mappings.common

import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.validRegistrationNumber
import play.api.data.Forms.nonEmptyText
import play.api.data.Mapping

object VehicleRegistrationNumber {
  final val MinLength = 2
  final val MaxLength = 8

  def registrationNumber: Mapping[String] = nonEmptyText(MinLength, MaxLength) verifying validRegistrationNumber
}