package models.domain.vrm_retention

import mappings.vrm_retention.VehicleLookup.KeeperLookupDetailsCacheKey
import models.domain.common.CacheKey
import play.api.libs.json.Json

final case class KeeperDetailsModel(title: String,
                                    firstName: String,
                                    lastName: String,
                                    addressLine1: String,
                                    addressLine2: String,
                                    addressLine3: String,
                                    addressLine4: String,
                                    postTown: String,
                                    postCode: String)

// TODO will be replaced by a combined dto when the get vehicle and keeper lookup ms plugged in
object KeeperDetailsModel {
  // Create a KeeperDetailsModel from the given replacementVRM. We do this in order get the data out of the response from micro-service call
  def fromResponse(title: String,
                   firstName: String,
                   lastName: String,
                   addressLine1: String,
                   addressLine2: String,
                   addressLine3: String,
                   addressLine4: String,
                   postTown: String,
                   postCode: String) =
    KeeperDetailsModel(title = title,
      firstName = firstName,
      lastName = lastName,
      addressLine1 = addressLine1,
      addressLine2 = addressLine2,
      addressLine3 = addressLine3,
      addressLine4 = addressLine4,
      postTown = postTown,
      postCode = postCode)

  implicit val JsonFormat = Json.format[KeeperDetailsModel]
  implicit val Key = CacheKey[KeeperDetailsModel](KeeperLookupDetailsCacheKey)
}

