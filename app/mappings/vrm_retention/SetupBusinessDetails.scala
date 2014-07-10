package mappings.vrm_retention

import constraints.vrm_retention.BusinessName
import play.api.data.Mapping
import utils.helpers.FormExtensions._

object SetupBusinessDetails {
  final val BusinessNameMaxLength = 58
  final val BusinessNameMinLength = 2
  final val BusinessNameId = "Business Name"

  final val BusinessPostcodeId = "Postcode"
  final val SetupBusinessDetailsCacheKey = "setupBusinessDetails"
  final val SubmitId = "submit"

  def businessName (minLength: Int = BusinessNameMinLength, maxLength: Int = BusinessNameMaxLength): Mapping[String] = {
    nonEmptyTextWithTransform(_.toUpperCase.trim)(minLength, maxLength) verifying BusinessName.validBusinessName
  }
}