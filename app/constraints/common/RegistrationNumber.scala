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

    val FiveSpaceCharPadding = "     "
    val FourSpaceCharPadding = "    "
    val ThreeSpaceCharPadding = "   "
    val TwoSpaceCharPadding = "  "
    val OneSpaceCharPadding = " "

    val Format1 = "([A-Z]{2}[0-9]{2}[A-Z]{3})".r // AA88AAA
    val Format2 = "([A-Z][0-9])".r // A9
    val Format3 = "([A-Z][0-9]{2})".r // A99
    val Format4 = "([A-Z][0-9]{3})".r // A999
    val Format5 = "([A-Z][0-9]{4})".r // A9999
    val Format6 = "([A-Z]{2}[0-9])".r // AA9
    val Format7 = "([A-Z]{2}[0-9]{2})".r // AA99
    val Format8 = "([A-Z]{2}[0-9]{3})".r // AA999
    val Format9 = "([A-Z]{2}[0-9]{4})".r // AA9999
    val Format10 = "([A-Z]{3}[0-9])".r // AAA9
    val Format11 = "([A-Z]{3}[0-9]{2})".r // AAA99
    val Format12 = "([A-Z]{3}[0-9]{3})".r // AAA999
    val Format13 = "([A-Z]{3}[0-9]{4})".r // AAA9999
    val Format14 = "([A-Z]{3}[0-9][A-Z])".r // AAA9Y
    val Format15 = "([A-Z]{3}[0-9]{2}[A-Z])".r // AAA99Y
    val Format16 = "([A-Z]{3}[0-9]{3}[A-Z])".r // AAA999Y
    val Format17 = "([0-9][A-Z])".r // 9A
    val Format18 = "([0]{3}[0-9][A-Z])".r // 0009  A
    val Format19 = "([0-9][A-Z]{2})".r // 9AA
    val Format20 = "([0]{3}[0-9][A-Z]{2})".r // 0009 AA
    val Format21 = "([0-9][A-Z]{3})".r // 9AAA
    val Format22 = "([0]{3}[0-9][A-Z]{3})".r // 0009AAA
    val Format23 = "([0-9]{2}[A-Z])".r // 99A
    val Format24 = "([0]{2}[0-9]{2}[A-Z])".r // 0099  A
    val Format25 = "([0-9]{2}[A-Z]{2})".r // 99AA
    val Format26 = "([0]{2}[0-9]{2}[A-Z]{2})".r // 0099 AA
    val Format27 = "([0-9]{2}[A-Z]{3})".r // 99AAA
    val Format28 = "([0]{2}[0-9]{2}[A-Z]{3})".r // 0099AAA
    val Format29 = "([0-9]{3}[A-Z])".r // 999A
    val Format30 = "([0][0-9]{3}[A-Z])".r // 0999  A
    val Format31 = "([0-9]{3}[A-Z]{2})".r // 999AA
    val Format32 = "([0][0-9]{3}[A-Z]{2})".r // 0999 AA
    val Format33 = "([0-9]{3}[A-Z]{3})".r // 9999AAA
    val Format34 = "([0][0-9]{3}[A-Z]{3})".r // 0999AAA
    val Format35 = "([0-9]{4}[A-Z])".r // 9999A
    val Format36 = "([0-9]{4}[A-Z]{2})".r // 9999AA
    val Format37 = "([A-Z][0-9][A-Z]{3})".r // Y9AAA
    val Format38 = "([A-Z][0-9]{2}[A-Z]{3})".r // Y99AAA
    val Format39 = "([A-Z][0-9]{3}[A-Z]{3})".r // Y999AAA

    vrm.toUpperCase.replace(OneSpaceCharPadding, "") match {
      case Format1(v) ⇒ v.substring(0, 4) + OneSpaceCharPadding + v.substring(4, 7)
      case Format2(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.charAt(1) + FiveSpaceCharPadding
      case Format3(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1, 3) + FourSpaceCharPadding
      case Format4(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1, 4) + ThreeSpaceCharPadding
      case Format5(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1, 5) + TwoSpaceCharPadding
      case Format6(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.charAt(2) + FourSpaceCharPadding
      case Format7(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2, 4) + ThreeSpaceCharPadding
      case Format8(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2, 5) + TwoSpaceCharPadding
      case Format9(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2, 6) + OneSpaceCharPadding
      case Format10(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.charAt(3) + ThreeSpaceCharPadding
      case Format11(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 5) + TwoSpaceCharPadding
      case Format12(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 6) + OneSpaceCharPadding
      case Format13(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 7)
      case Format14(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 5) + TwoSpaceCharPadding
      case Format15(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 6) + OneSpaceCharPadding
      case Format16(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3, 7)
      case Format17(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.charAt(1) + FiveSpaceCharPadding
      case Format18(v) ⇒ "" + v.charAt(3) + OneSpaceCharPadding + v.charAt(4) + FiveSpaceCharPadding
      case Format19(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1,3) + FourSpaceCharPadding
      case Format20(v) ⇒ "" + v.charAt(3) + OneSpaceCharPadding + v.substring(4,6) + FourSpaceCharPadding
      case Format21(v) ⇒ "" + v.charAt(0) + OneSpaceCharPadding + v.substring(1,4) + ThreeSpaceCharPadding
      case Format22(v) ⇒ "" + v.charAt(3) + OneSpaceCharPadding + v.substring(4,7) + ThreeSpaceCharPadding
      case Format23(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.charAt(2) + FourSpaceCharPadding
      case Format24(v) ⇒ v.substring(2,4) + OneSpaceCharPadding + v.charAt(4) + FourSpaceCharPadding
      case Format25(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2,4) + ThreeSpaceCharPadding
      case Format26(v) ⇒ v.substring(2,4) + OneSpaceCharPadding + v.substring(4,6) + ThreeSpaceCharPadding
      case Format27(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2,5) + TwoSpaceCharPadding
      case Format28(v) ⇒ v.substring(2,4) + OneSpaceCharPadding + v.substring(4,7) + TwoSpaceCharPadding
      case Format29(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.charAt(3) + ThreeSpaceCharPadding
      case Format30(v) ⇒ v.substring(1,4) + OneSpaceCharPadding + v.charAt(4) + ThreeSpaceCharPadding
      case Format31(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3,5) + TwoSpaceCharPadding
      case Format32(v) ⇒ v.substring(1,4) + OneSpaceCharPadding + v.substring(4,6) + TwoSpaceCharPadding
      case Format33(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3,6) + OneSpaceCharPadding
      case Format34(v) ⇒ v.substring(1,4) + OneSpaceCharPadding + v.substring(4,7) + OneSpaceCharPadding
      case Format35(v) ⇒ v.substring(0,4) + OneSpaceCharPadding + v.charAt(4) + TwoSpaceCharPadding
      case Format36(v) ⇒ v.substring(0,4) + OneSpaceCharPadding + v.substring(4,6) + OneSpaceCharPadding
      case Format37(v) ⇒ v.substring(0,2) + OneSpaceCharPadding + v.substring(2,5) + TwoSpaceCharPadding
      case Format38(v) ⇒ v.substring(0,3) + OneSpaceCharPadding + v.substring(3,6) + OneSpaceCharPadding
      case Format39(v) ⇒ v.substring(0,4) + OneSpaceCharPadding + v.substring(4,7)
      case _ ⇒ vrm
    }
  }
}