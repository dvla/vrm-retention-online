package models

import play.api.data.Forms._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.DropDown
import views.vrm_retention.BusinessChooseYourAddress.AddressSelectId

final case class BusinessChooseYourAddressFormModel(uprnSelected: String)

object BusinessChooseYourAddressFormModel {

  implicit val JsonFormat = Json.format[BusinessChooseYourAddressFormModel]
  final val BusinessChooseYourAddressCacheKey = "businessChooseYourAddress"
  implicit val Key = CacheKey[BusinessChooseYourAddressFormModel](value = BusinessChooseYourAddressCacheKey)

  object Form {

    final val Mapping = mapping(
      /* We cannot apply constraints to this drop down as it is populated by web call to an address lookup service.
      We would need the request here to get the cookie.
      Validation is done when we make a second web call with the UPRN,
      so if a bad guy is injecting a non-existent UPRN then it will fail at that step instead */
      AddressSelectId -> DropDown.addressDropDown
    )(BusinessChooseYourAddressFormModel.apply)(BusinessChooseYourAddressFormModel.unapply)
  }

}
