package mappings.common

import models.domain.common.AddressLinesModel
import play.api.data.Forms.{mapping, optional}
import play.api.data.Mapping
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.{nonEmptyTextWithTransform, textWithTransform, trimNonWhiteListedChars}

object AddressLines {
  final val AddressLinesId = "addressLines"
  final val BuildingNameOrNumberId = "buildingNameOrNumber"
  final val Line2Id = "line2"
  final val Line3Id = "line3"
  final val Line4Id = "line4"
  final val PostTownId = "postTown"
  final val BuildingNameOrNumberMinLength = 4
  final val PostTownMinLength = 3
  final val LineMaxLength = 30
  final val MaxLengthOfLinesConcatenated = 120

  final val AddressLinesCacheKey = "addressLines"
  final val BuildingNameOrNumberHolder = "No building name/num supplied"

  final val BuildingNameOrNumberIndex = 0
  final val Line2Index = 1
  final val Line3Index = 2
  final val Line4Index = 3
  final val emptyLine = ""

  private def fieldTransform(s: String) = trimNonWhiteListedChars("""[A-Za-z0-9]""")(s.toUpperCase)

  def addressLines: Mapping[AddressLinesModel] = mapping(
    BuildingNameOrNumberId ->
      nonEmptyTextWithTransform(fieldTransform)(minLength = BuildingNameOrNumberMinLength, maxLength = LineMaxLength),
    Line2Id -> optional(textWithTransform(fieldTransform)(maxLength = LineMaxLength)),
    Line3Id -> optional(textWithTransform(fieldTransform)(maxLength = LineMaxLength)),
    Line4Id -> optional(textWithTransform(fieldTransform)(maxLength = LineMaxLength)),
    PostTownId -> nonEmptyTextWithTransform(fieldTransform)(minLength = PostTownMinLength, maxLength = LineMaxLength)
  )(AddressLinesModel.apply)(AddressLinesModel.unapply)
}