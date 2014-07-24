package constraints.common

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object Postcode {

  def validPostcode: Constraint[String] = Constraint("constraint.restricted.validPostcode") { input =>
    val whitelist =
      """^
        |(?i)(GIR 0AA)|
        |((([A-Z][0-9][0-9]?)|
        |(([A-Z][A-HJ-Y][0-9][0-9]?)|
        |(([A-Z][0-9][A-Z])|
        |([A-Z][A-HJ-Y][0-9]?[A-Z]))))[ ]?[0-9][A-Z]{2})
        |$""".stripMargin.replace("\n", "").r

    if (whitelist.pattern.matcher(input).matches) Valid
    else Invalid(ValidationError("error.restricted.validPostcode"))
  }

  def formatPostcode(postcode: String): String = {

    val SpaceCharDelimiter = " "
    val Format1 = "([A-Z][0-9][0-9][A-Z][A-Z])".r // A99AA
    val Format2 = "([A-Z][0][0-9][0-9][A-Z][A-Z])".r // A099AA
    val Format3 = "([A-Z][0-9][0-9][0-9][A-Z][A-Z])".r // A999AA
    val Format4 = "([A-Z][0-9][A-Z][0-9][A-Z][A-Z])".r // A9A9AA
    val Format5 = "([A-Z][A-Z][0-9][0-9][A-Z][A-Z])".r // AA99AA
    val Format6 = "([A-Z][A-Z][0][0-9][0-9][A-Z][A-Z])".r // AA099AA
    val Format7 = "([A-Z][A-Z][0-9][0-9][0-9][A-Z][A-Z])".r // AA999AA
    val Format8 = "([A-Z][A-Z][0-9][A-Z][0-9][A-Z][A-Z])".r // AA9A9AA

    postcode.toUpperCase.replace(SpaceCharDelimiter,"") match {
      case Format1(p) ⇒ p.substring(0, 2) + SpaceCharDelimiter + p.substring(2, 5)
      case Format2(p) ⇒ "" + p.charAt(0) + p.charAt(2) + SpaceCharDelimiter + p.substring(3, 6)
      case Format3(p) ⇒ p.substring(0, 3) + SpaceCharDelimiter + p.substring(3, 6)
      case Format4(p) ⇒ p.substring(0, 3) + SpaceCharDelimiter + p.substring(3, 6)
      case Format5(p) ⇒ p.substring(0, 3) + SpaceCharDelimiter + p.substring(3, 6)
      case Format6(p) ⇒ p.substring(0, 2) + p.charAt(3) + SpaceCharDelimiter + p.substring(4, 7)
      case Format7(p) ⇒ p.substring(0, 4) + SpaceCharDelimiter + p.substring(4, 7)
      case Format8(p) ⇒ p.substring(0, 4) + SpaceCharDelimiter + p.substring(4, 7)
      case _ ⇒ postcode
    }
  }
}