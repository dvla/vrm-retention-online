package controllers

import helpers.UnitSpec
import models.SetupBusinessDetailsFormModel
import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.mappings.AddressPicker
//import views.vrm_retention.Confirm._
import views.vrm_retention.SetupBusinessDetails._
import webserviceclients.fakes.AddressLookupServiceConstants._
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}

class SetUpBusinessDetailsFormSpec extends UnitSpec {

  "form" should {

    "accept if form is valid with all fields filled in" in {
      val model = formWithValidDefaults(
        traderBusinessName = TraderBusinessNameValid,
        traderBusinessContact = TraderBusinessContactValid,
        traderBusinessEmail = TraderBusinessEmailValid,
        traderPostcode = PostcodeValid
      ).get
      model.name should equal(TraderBusinessNameValid.toUpperCase)
      model.contact should equal(TraderBusinessContactValid.toUpperCase)
      model.email should equal(TraderBusinessEmailValid)
      model.address.postCode should equal(PostcodeValid)
      model.address.searchFields.postCode should equal(Some(SearchPostcodeValid))
      model.address.streetAddress1 should equal(BusinessAddressLine1Valid)
      model.address.streetAddress2 should equal(Some(BusinessAddressLine2Valid))
      model.address.streetAddress3 should equal(None)
      model.address.postTown should equal(PostTownValid)
    }
/*
      val model = formWithValidDefaults(
        traderBusinessName = TraderBusinessNameValid,
        traderBusinessContact = TraderBusinessContactValid,
        traderBusinessEmail = TraderBusinessEmailValid,
        traderPostcode = PostcodeValid
      ).get
      model.name should equal(TraderBusinessNameValid.toUpperCase)
      model.contact should equal(TraderBusinessContactValid.toUpperCase)
      model.email should equal(TraderBusinessEmailValid)
      model.postcode should equal(PostcodeValid)
*/
  }

  "dealerName" should {

    "reject if trader business name is blank" in {
      // IMPORTANT: The messages being returned by the form validation are overridden by the Controller
      val errors = formWithValidDefaults(traderBusinessName = "").errors
      errors should have length 3
      errors.head.key should equal(BusinessNameId)
      errors.head.message should equal("error.minLength")
      errors(1).key should equal(BusinessNameId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(BusinessNameId)
      errors(2).message should equal("error.validBusinessName")
    }

    "reject if trader business name is less than minimum length" in {
      formWithValidDefaults(traderBusinessName = "A").errors should have length 1
    }

    "reject if trader business name is more than the maximum length" in {
      formWithValidDefaults(traderBusinessName = "A" * 101).errors should have length 1
    }

    "accept if trader business name is valid" in {
      formWithValidDefaults(traderBusinessName = TraderBusinessNameValid, traderPostcode = PostcodeValid).
        get.name should equal(TraderBusinessNameValid.toUpperCase)
    }
  }
/*
  "postcode" should {

    "reject if trader postcode is empty" in {
      // IMPORTANT: The messages being returned by the form validation are overridden by the Controller
      val errors = formWithValidDefaults(traderPostcode = "").errors
      errors should have length 3
      errors(0).key should equal(BusinessPostcodeId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(BusinessPostcodeId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(BusinessPostcodeId)
      errors(2).message should equal("error.restricted.validPostcode")
    }

    "reject if trader postcode is less than the minimum length" in {
      formWithValidDefaults(traderPostcode = "M15A").errors should have length 2
    }

    "reject if trader postcode is more than the maximum length" in {
      formWithValidDefaults(traderPostcode = "SA99 1DDD").errors should have length 2
    }

    "reject if trader postcode contains special characters" in {
      formWithValidDefaults(traderPostcode = "SA99 1D$").errors should have length 1
    }

    "reject if trader postcode contains an incorrect format" in {
      formWithValidDefaults(traderPostcode = "SAR99").errors should have length 1
    }
  }
*/
  // TODO: ian delete this
/*
  private def formWithValidDefaults(traderBusinessName: String = TraderBusinessNameValid,
                                    traderBusinessContact: String = TraderBusinessContactValid,
                                    traderBusinessEmail: String = TraderBusinessEmailValid,
                                    traderPostcode: String = PostcodeValid) = {
    Form(SetupBusinessDetailsFormModel.Form.Mapping).bind(
      Map(
        BusinessNameId -> traderBusinessName,
        BusinessContactId -> traderBusinessContact,
        s"$BusinessEmailId.$EmailId" -> traderBusinessEmail,
        s"$BusinessEmailId.$EmailVerifyId" -> traderBusinessEmail,
        BusinessPostcodeId -> traderPostcode
      )
    )
  }
 */

  private def formWithValidDefaults(traderBusinessName: String = TraderBusinessNameValid,
                                    traderBusinessContact: String = TraderBusinessContactValid,
                                    traderBusinessEmail: String = TraderBusinessEmailValid,
                                    traderPostcode: String = PostcodeValid,
                                    searchPostCode: String = SearchPostcodeValid,
                                    addressListSelect: String = AddressListSelectValid,
                                    showSearchFields: Boolean = true,
                                    showAddressSelect: Boolean = true,
                                    showAddressFields: Boolean = true,
                                    addressLine1: String = BusinessAddressLine1Valid,
                                    addressLine2: String = BusinessAddressLine2Valid,
                                    addressLine3: String = BusinessAddressLine3Valid,
                                    postTown: String = PostTownValid,
                                    saveDetails: Boolean = true) = {
    val data = Map(
      BusinessNameId -> traderBusinessName,
      BusinessContactId -> traderBusinessContact,
      s"$BusinessEmailId.$EmailId" -> traderBusinessEmail,
      s"$BusinessEmailId.$EmailVerifyId" -> traderBusinessEmail,
      s"$BusinessAddressId.${AddressPicker.SearchByPostcodeField}" -> searchPostCode,
      s"$BusinessAddressId.${AddressPicker.AddressListSelect}" -> addressListSelect,
      s"$BusinessAddressId.${AddressPicker.ShowSearchFields}" -> showSearchFields.toString,
      s"$BusinessAddressId.${AddressPicker.ShowAddressSelect}" -> showAddressSelect.toString,
      s"$BusinessAddressId.${AddressPicker.ShowAddressFields}" -> showAddressFields.toString,
      s"$BusinessAddressId.${AddressPicker.AddressLine1Id}" -> addressLine1,
      s"$BusinessAddressId.${AddressPicker.AddressLine2Id}" -> addressLine2,
      s"$BusinessAddressId.${AddressPicker.AddressLine3Id}" -> addressLine3,
      s"$BusinessAddressId.${AddressPicker.PostTownId}" -> postTown,
      s"$BusinessAddressId.${AddressPicker.PostcodeId}" -> traderPostcode
    ) ++ (if (saveDetails) Map(s"$BusinessAddressId.${AddressPicker.RememberId}" -> "true")
    else Seq.empty[(String, String)])

    Form(SetupBusinessDetailsFormModel.Form.Mapping).bind(data)
  }

}
