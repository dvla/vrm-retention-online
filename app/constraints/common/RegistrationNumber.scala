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
    val FiveSpaceCharPadding = " " * 5
    val FourSpaceCharPadding = " " * 4
    val ThreeSpaceCharPadding = " " * 3
    val TwoSpaceCharPadding = " " * 2
    val OneSpaceCharPadding = " " * 1

    val AA99AAA = "([A-Z]{2}[0-9]{2}[A-Z]{3})".r // AA99AAA
    val A9 = "([A-Z][0-9])".r // A9
    val A99 = "([A-Z][0-9]{2})".r // A99
    val A999 = "([A-Z][0-9]{3})".r // A999
    val A9999 = "([A-Z][0-9]{4})".r // A9999
    val AA9 = "([A-Z]{2}[0-9])".r // AA9
    val AA99 = "([A-Z]{2}[0-9]{2})".r // AA99
    val AA999 = "([A-Z]{2}[0-9]{3})".r // AA999
    val AA9999 = "([A-Z]{2}[0-9]{4})".r // AA9999
    val AAA9 = "([A-Z]{3}[0-9])".r // AAA9
    val AAA99 = "([A-Z]{3}[0-9]{2})".r // AAA99
    val AAA999 = "([A-Z]{3}[0-9]{3})".r // AAA999
    val AAA9999 = "([A-Z]{3}[0-9]{4})".r // AAA9999
    val AAA9Y = "([A-Z]{3}[0-9][A-Z])".r // AAA9Y
    val AAA99Y = "([A-Z]{3}[0-9]{2}[A-Z])".r // AAA99Y
    val AAA999Y = "([A-Z]{3}[0-9]{3}[A-Z])".r // AAA999Y
    val `9A` = "([0-9][A-Z])".r // 9A
    val `0009  A` = "([0]{3}[0-9][A-Z])".r // 0009  A
    val `9AA` = "([0-9][A-Z]{2})".r // 9AA
    val `0009 AA` = "([0]{3}[0-9][A-Z]{2})".r // 0009 AA
    val `9AAA` = "([0-9][A-Z]{3})".r // 9AAA
    val `0009AAA` = "([0]{3}[0-9][A-Z]{3})".r // 0009AAA
    val `99A` = "([0-9]{2}[A-Z])".r // 99A
    val `0099  A` = "([0]{2}[0-9]{2}[A-Z])".r // 0099  A
    val `99AA` = "([0-9]{2}[A-Z]{2})".r // 99AA
    val `0099 AA` = "([0]{2}[0-9]{2}[A-Z]{2})".r // 0099 AA
    val `99AAA` = "([0-9]{2}[A-Z]{3})".r // 99AAA
    val `0099AAA` = "([0]{2}[0-9]{2}[A-Z]{3})".r // 0099AAA
    val `999A` = "([0-9]{3}[A-Z])".r // 999A
    val `0999  A` = "([0][0-9]{3}[A-Z])".r // 0999  A
    val `999AA` = "([0-9]{3}[A-Z]{2})".r // 999AA
    val `0999 AA` = "([0][0-9]{3}[A-Z]{2})".r // 0999 AA
    val `9999AAA` = "([0-9]{3}[A-Z]{3})".r // 9999AAA
    val `0999AAA` = "([0][0-9]{3}[A-Z]{3})".r // 0999AAA
    val `9999A` = "([0-9]{4}[A-Z])".r // 9999A
    val `9999AA` = "([0-9]{4}[A-Z]{2})".r // 9999AA
    val `A9AAA` = "([A-Z][0-9][A-Z]{3})".r // A9AAA
    val `A99AAA` = "([A-Z][0-9]{2}[A-Z]{3})".r // A99AAA
    val `A999AAA` = "([A-Z][0-9]{3}[A-Z]{3})".r // A999AAA

    vrm.toUpperCase.replace(OneSpaceCharPadding, "") match {
      case AA99AAA(v) ⇒ v.substring(0, 4) + OneSpaceCharPadding + v.substring(4, 7)
      case A9(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.charAt(1) + FiveSpaceCharPadding
      case A99(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1, 3) + FourSpaceCharPadding
      case A999(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1, 4) + ThreeSpaceCharPadding
      case A9999(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1, 5) + TwoSpaceCharPadding
      case AA9(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.charAt(2) + FourSpaceCharPadding
      case AA99(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2, 4) + ThreeSpaceCharPadding
      case AA999(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2, 5) + TwoSpaceCharPadding
      case AA9999(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2, 6) + OneSpaceCharPadding
      case AAA9(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.charAt(3) + ThreeSpaceCharPadding
      case AAA99(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 5) + TwoSpaceCharPadding
      case AAA999(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 6) + OneSpaceCharPadding
      case AAA9999(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 7)
      case AAA9Y(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 5) + TwoSpaceCharPadding
      case AAA99Y(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 6) + OneSpaceCharPadding
      case AAA999Y(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 7)
      case `9A`(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.charAt(1) + FiveSpaceCharPadding
      case `0009  A`(v) ⇒ "" + v.charAt(3) + OneSpaceCharPadding + v.charAt(4) + FiveSpaceCharPadding
      case `9AA`(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1,3) + FourSpaceCharPadding
      case `0009 AA`(v) ⇒ "" + v.charAt(3) + OneSpaceCharPadding + v.substring(4,6) + FourSpaceCharPadding
      case `9AAA`(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1,4) + ThreeSpaceCharPadding
      case `0009AAA`(v) ⇒ "" + v.charAt(3) + OneSpaceCharPadding + v.substring(4,7) + ThreeSpaceCharPadding
      case `99A`(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.charAt(2) + FourSpaceCharPadding
      case `0099  A`(v) ⇒ v.substring(2,4) + OneSpaceCharPadding + v.charAt(4) + FourSpaceCharPadding
      case `99AA`(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2,4) + ThreeSpaceCharPadding
      case `0099 AA`(v) ⇒ v.substring(2,4) + OneSpaceCharPadding + v.substring(4,6) + ThreeSpaceCharPadding
      case `99AAA`(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2,5) + TwoSpaceCharPadding
      case `0099AAA`(v) ⇒ v.substring(2,4) + OneSpaceCharPadding + v.substring(4,7) + TwoSpaceCharPadding
      case `999A`(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.charAt(3) + ThreeSpaceCharPadding
      case `0999  A`(v) ⇒ v.substring(1,4) + OneSpaceCharPadding + v.charAt(4) + ThreeSpaceCharPadding
      case `999AA`(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3,5) + TwoSpaceCharPadding
      case `0999 AA`(v) ⇒ v.substring(1,4) + OneSpaceCharPadding + v.substring(4,6) + TwoSpaceCharPadding
      case `9999AAA`(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3,6) + OneSpaceCharPadding
      case `0999AAA`(v) ⇒ v.substring(1,4) + OneSpaceCharPadding + v.substring(4,7) + OneSpaceCharPadding
      case `9999A`(v) ⇒ v.substring(0,4) + OneSpaceCharPadding + v.charAt(4) + TwoSpaceCharPadding
      case `9999AA`(v) ⇒ v.substring(0,4) + OneSpaceCharPadding + v.substring(4,6) + OneSpaceCharPadding
      case `A9AAA`(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2,5) + TwoSpaceCharPadding
      case `A99AAA`(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3,6) + OneSpaceCharPadding
      case `A999AAA`(v) ⇒ v.substring(0,4) + OneSpaceCharPadding + v.substring(4,7)
      case _ ⇒ vrm
    }
  }
}