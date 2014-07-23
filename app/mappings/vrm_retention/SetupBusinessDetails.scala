package mappings.vrm_retention

import constraints.vrm_retention.BusinessName
import play.api.data.Mapping
import utils.helpers.FormExtensions.nonEmptyTextWithTransform

object SetupBusinessDetails {

  final val BusinessNameMaxLength = 58
  final val BusinessNameMinLength = 2
  final val BusinessContactMaxLength = 58
  final val BusinessContactMinLength = 2
  final val BusinessEmailMaxLength = 58
  final val BusinessEmailMinLength = 2
  final val BusinessNameId = "Business Name"
  final val BusinessContactId = "Business Contact"
  final val BusinessEmailId = "Business@Email.com"
  final val BusinessPostcodeId = "Postcode"
  final val SetupBusinessDetailsCacheKey = "setupBusinessDetails"
  final val SubmitId = "submit"

  def businessName(minLength: Int = BusinessNameMinLength, maxLength: Int = BusinessNameMaxLength): Mapping[String] = {
    nonEmptyTextWithTransform(_.toUpperCase.trim)(minLength, maxLength) verifying BusinessName.validBusinessName
  }

  def businessContact(minLength: Int = BusinessContactMinLength, maxLength: Int = BusinessContactMaxLength): Mapping[String] = {
    nonEmptyTextWithTransform(_.toUpperCase.trim)(minLength, maxLength) verifying BusinessName.validBusinessName
  }
}