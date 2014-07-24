package mappings.common

import constraints.common.RegistrationNumber
import RegistrationNumber.validRegistrationNumber
import play.api.data.Forms.nonEmptyText
import play.api.data.Mapping

object VehicleRegistrationNumber {
  final val MinLength = 2
  final val MaxLength = 8

  def registrationNumber: Mapping[String] = nonEmptyText(MinLength, MaxLength) verifying validRegistrationNumber
}