package controllers

import helpers.UnitSpec
import play.api.data.Form
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnValid
import models.BusinessChooseYourAddressFormModel
import views.vrm_retention.BusinessChooseYourAddress.AddressSelectId

final class BusinessChooseYourAddressFormSpec extends UnitSpec {

  "form" should {

    "accept when all fields contain valid responses" in {
      formWithValidDefaults().get.uprnSelected should equal(traderUprnValid.toString)
    }
  }

  "addressSelect" should {

    "reject if empty" in {
      val errors = formWithValidDefaults(addressSelected = "").errors
      errors.length should equal(1)
      errors(0).key should equal(AddressSelectId)
      errors(0).message should equal("error.required")
    }
  }

  private def formWithValidDefaults(addressSelected: String = traderUprnValid.toString) = {
    Form(BusinessChooseYourAddressFormModel.Form.Mapping).bind(
      Map(AddressSelectId -> addressSelected)
    )
  }
}