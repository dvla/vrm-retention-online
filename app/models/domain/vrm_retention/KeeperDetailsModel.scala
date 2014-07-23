package models.domain.vrm_retention

import mappings.vrm_retention.VehicleLookup.KeeperLookupDetailsCacheKey
import models.domain.common.{AddressAndPostcodeModel, AddressLinesModel, AddressViewModel, CacheKey}
import play.api.libs.json.Json

final case class KeeperDetailsModel(title: String,
                                    firstName: String,
                                    lastName: String,
                                    address: AddressViewModel)

// TODO will be replaced by a combined dto when the get vehicle and keeper lookup ms plugged in
object KeeperDetailsModel {

  // Create a KeeperDetailsModel from the given replacementVRM. We do this in order get the data out of the response from micro-service call
  def fromResponse(title: String,
                   firstName: String,
                   lastName: String,
                   addressLine1: String,
                   addressLine2: String,
                   postTown: String,
                   postCode: String) = {

    // TODO need to enhance this model to cater for a third and fourth address line
    val addressViewModel = {
      val addressLineModel = AddressLinesModel(addressLine1, Some(addressLine2), None, postTown)
      val addressAndPostcodeModel = AddressAndPostcodeModel.apply(None, addressLineModel)
      AddressViewModel.from(addressAndPostcodeModel, postCode)
    }

    KeeperDetailsModel(title = title,
      firstName = firstName,
      lastName = lastName,
      address = addressViewModel)
  }

  implicit val JsonFormat = Json.format[KeeperDetailsModel]
  implicit val Key = CacheKey[KeeperDetailsModel](KeeperLookupDetailsCacheKey)
}