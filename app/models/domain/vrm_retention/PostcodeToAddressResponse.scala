package models.domain.vrm_retention

import play.api.libs.json.Json

final case class PostcodeToAddressResponse(addresses: Seq[UprnAddressPair])

object PostcodeToAddressResponse{
  implicit val JsonFormat = Json.format[PostcodeToAddressResponse]
}