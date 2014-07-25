package constraints.common

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object RegistrationNumber {

  def validRegistrationNumber: Constraint[String] = Constraint("constraint.restricted.validVrn") { input =>
    val whitelist =
      """^
        |([A-Za-z]{3}[0-9]{1,4})|
        |([A-Za-z][0-9]{1,3}[A-Za-z]{3})|
        |([A-Za-z]{3}[0-9]{1,3}[A-Za-z])|
        |([A-Za-z]{2}[0-9]{2}[A-Za-z]{3})|
        |([A-Za-z]{1,3}[0-9]{1,3})|
        |([0-9]{1,4}[A-Za-z]{1,3})|
        |([A-Za-z]{1,2}[0-9]{1,4})
        |$""".stripMargin.replace("\n", "").r

    if (whitelist.pattern.matcher(input.replace(" ", "")).matches) Valid
    else Invalid(ValidationError("error.restricted.validVrnOnly"))
  }

  def formatVrm(vrm: String): String = {

    val SpaceCharDelimiter = " "
    val FiveSpaceCharPadding = "     "
    val FourSpaceCharPadding = "    "
    val ThreeSpaceCharPadding = "   "
    val TwoSpaceCharPadding = "  "
    val Format1 = "([A-Z]{2}[0-9]{2}[A-Z]{3})".r // AA88AAA
    val Format2 = "([A-Z][0-9])".r // A9
    val Format3 = "([A-Z][0-9]{2})".r // A99
    val Format4 = "([A-Z][0-9]{3})".r // A999
    val Format5 = "([A-Z][0-9]{4})".r // A9999

    vrm.toUpperCase.replace(SpaceCharDelimiter, "") match {
      case Format1(v) ⇒ v.substring(0, 4) + SpaceCharDelimiter + v.substring(4, 7)
      case Format2(v) ⇒ "" + v.charAt(0) + SpaceCharDelimiter + v.charAt(1) + FiveSpaceCharPadding
      case Format3(v) ⇒ "" + v.charAt(0) + SpaceCharDelimiter + v.substring(1, 3) + FourSpaceCharPadding
      case Format4(v) ⇒ "" + v.charAt(0) + SpaceCharDelimiter + v.substring(1, 4) + ThreeSpaceCharPadding
      case Format5(v) ⇒ "" + v.charAt(0) + SpaceCharDelimiter + v.substring(1, 5) + TwoSpaceCharPadding
      case _ ⇒ vrm
    }
  }
}