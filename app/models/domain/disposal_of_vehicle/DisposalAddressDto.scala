package models.domain.disposal_of_vehicle

import mappings.common.AddressLines.BuildingNameOrNumberIndex
import mappings.common.AddressLines.BuildingNameOrNumberHolder
import mappings.common.AddressLines.emptyLine
import mappings.common.AddressLines.Line2Index
import mappings.common.AddressLines.Line3Index
import mappings.common.AddressLines.LineMaxLength
import scala.annotation.tailrec
import models.domain.common.AddressViewModel

case class DisposalAddressDto(line: Seq[String], postTown: Option[String], postCode: String, uprn: Option[Long])

object DisposalAddressDto {
  import play.api.libs.json.Json
  implicit val addressDto = Json.writes[DisposalAddressDto]

  def from(addressViewModel: AddressViewModel): DisposalAddressDto = {
    val trimRequired = linesOverMaxLength(addressViewModel.address)
    val addressMandatoryLines =
      if (addressViewModel.address.size == 2)
        AddressViewModel(
          addressViewModel.uprn,
          Seq(BuildingNameOrNumberHolder) ++ addressViewModel.address
        )
      else addressViewModel

    if (trimRequired) rebuildDisposalAddressDto(addressMandatoryLines)
    else buildStandardDisposalAddressDto(addressMandatoryLines)
  }

  @tailrec
  private def linesOverMaxLength(address: Seq[String]): Boolean =
    if (address.isEmpty) false
    else if (address.head.size > LineMaxLength) true
    else linesOverMaxLength(address.tail)

  private def buildStandardDisposalAddressDto(addressViewModel: AddressViewModel): DisposalAddressDto = {
    val postcode = addressViewModel.address.last.replace(" ","")
    val postTown = Some(addressViewModel.address.takeRight(2).head)
    DisposalAddressDto(addressViewModel.address.dropRight(2), postTown , postcode, addressViewModel.uprn)
  }

  private def rebuildDisposalAddressDto(addressViewModel: AddressViewModel): DisposalAddressDto = {
    val address = assignEmptyLines(addressViewModel.address)
    val isLine2Empty = address(Line2Index) == emptyLine
    val isLine3Empty = address(Line3Index) == emptyLine
    val isBuildingNameOrNumberOverMax = address(BuildingNameOrNumberIndex).size > LineMaxLength
    val isLine2OverMax = address(Line2Index).size > LineMaxLength

    val amendedAddressLines = addressLinesDecider(
      isBuildingNameOrNumberOverMax,
      isLine2OverMax,
      isLine2Empty,
      isLine3Empty,
      address
    )
    val legacyAddressLines = trimLines(amendedAddressLines.dropRight(1), Nil)

    val postcode = addressViewModel.address.last.replaceAll(" ","")
    DisposalAddressDto(legacyAddressLines.dropRight(1), Some(legacyAddressLines.last), postcode, addressViewModel.uprn)
  }

  private def assignEmptyLines(address: Seq[String]) : Seq[String] = {
    address.size match { //every address returned by OS contains at least one address line and a postcode
      case 3 => Seq(address(BuildingNameOrNumberIndex)) ++ Seq(emptyLine) ++ address.tail
      case 4 => Seq(address(BuildingNameOrNumberIndex)) ++ Seq(address(Line2Index)) ++ Seq(emptyLine) ++ address.drop(2)
      case _ => address
    }
  }

  private def addressLinesDecider(isBuildingNameOrNumberOverMax: Boolean,
                                  isLine2OverMax: Boolean,
                                  isLine2Empty: Boolean,
                                  isLine3Empty: Boolean,
                                  address: Seq[String]) : Seq[String]= {
    (isBuildingNameOrNumberOverMax, isLine2OverMax, isLine2Empty, isLine3Empty) match {
      case (true, _, true, _) => Seq(address(BuildingNameOrNumberIndex).substring(0, LineMaxLength)) ++
                                 Seq(address(BuildingNameOrNumberIndex).substring(LineMaxLength)) ++
                                 address.drop(2)
      case (true, _, false, true) => Seq(address(BuildingNameOrNumberIndex).substring(0, LineMaxLength)) ++
                                 Seq(address(BuildingNameOrNumberIndex).substring(LineMaxLength)) ++
                                 Seq(address(Line2Index)) ++
                                 address.drop(3)
      case (false, true, false, true) => Seq(address(BuildingNameOrNumberIndex)) ++
                                         Seq(address(Line2Index).substring(0, LineMaxLength)) ++
                                         Seq(address(Line2Index).substring(LineMaxLength)) ++
                                         address.drop(3)
      case (_) => address
    }
  }
  
  @tailrec
  private def trimLines(address: Seq[String], accumulatedAddress: Seq[String]) : Seq[String] = {
    if (address.isEmpty) accumulatedAddress
    else if (address.head.size > LineMaxLength)
      trimLines(address.tail, accumulatedAddress :+ address.head.substring(0, LineMaxLength))
    else trimLines(address.tail, accumulatedAddress :+ address.head)
  }
}