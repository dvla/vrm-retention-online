package views.constraints

import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.pattern

/**
 * Postcode constraint for VPDS keeper postcodes ie exactly what is on the V5C
 */
object Postcode {

  def validPostcode: Constraint[String] = pattern(
    regex =
      """^[A-Z0-9 ]*$""".stripMargin.replace("\n", "").r,
    name = "constraint.restricted.validPostcode",
    error = "error.restricted.validV5CPostcode")
}