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

//  def formatVrm(vrm: String): String = {
//
//    val SpaceCharDelimiter = " "
//    val Format1 = "([A-Z][0-9][0-9][A-Z][A-Z])".r // A99AA
//    val Format2 = "([A-Z][0][0-9][0-9][A-Z][A-Z])".r // A099AA
//    val Format3 = "([A-Z][0-9][0-9][0-9][A-Z][A-Z])".r // A999AA
//    val Format4 = "([A-Z][0-9][A-Z][0-9][A-Z][A-Z])".r // A9A9AA
//    val Format5 = "([A-Z][A-Z][0-9][0-9][A-Z][A-Z])".r // AA99AA
//    val Format6 = "([A-Z][A-Z][0][0-9][0-9][A-Z][A-Z])".r // AA099AA
//    val Format7 = "([A-Z][A-Z][0-9][0-9][0-9][A-Z][A-Z])".r // AA999AA
//    val Format8 = "([A-Z][A-Z][0-9][A-Z][0-9][A-Z][A-Z])".r // AA9A9AA
//
//    vrm.trim.toUpperCase match {
//      case Format1(v) ⇒ v.substring(0, 4) + " " + v.substring(4, 7)
//      case Format2(v) ⇒ v.substring(0, 4) + " " + v.substring(4, 7)
//      case Format3(v) ⇒ v.charAt(0) + " " + v.charAt(1) + "     "
//      case Format4(v) ⇒ v.charAt(0) + " " + v.substring(1,3) + "    "
//      case Format5(v) ⇒ v.charAt(0) + " " + v.substring(1,3) + "    "
//      case Format6(v) ⇒ v.charAt(0) + " " + v.substring(1,3) + "    "
//      case Format7(v) ⇒ v.charAt(0) + " " + v.substring(1,3) + "    "
//      case Format8(v) ⇒ v.charAt(0) + " " + v.substring(1,3) + "    "
//      case _ ⇒ vrm
//    }
//  }
}