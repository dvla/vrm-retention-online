package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel

final case class EnterAddressManuallyModel(addressAndPostcodeViewModel: AddressAndPostcodeViewModel)

object EnterAddressManuallyModel {

  implicit val JsonFormat = Json.format[EnterAddressManuallyModel]
  final val EnterAddressManuallyCacheKey = "enterAddressManually"
  implicit val Key = CacheKey[EnterAddressManuallyModel](EnterAddressManuallyCacheKey)

  object Form {

    final val AddressAndPostcodeId = "addressAndPostcode"
    final val Mapping = mapping(
      AddressAndPostcodeId -> AddressAndPostcodeViewModel.Form.Mapping
    )(EnterAddressManuallyModel.apply)(EnterAddressManuallyModel.unapply)
  }

}