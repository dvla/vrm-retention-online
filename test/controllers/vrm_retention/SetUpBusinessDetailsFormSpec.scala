package controllers.vrm_retention

import helpers.UnitSpec
import mappings.vrm_retention.SetupBusinessDetails._
import composition.TestModule.AddressLookupServiceConstants._

final class SetUpBusinessDetailsFormSpec extends UnitSpec {

  "form" should {

    "accept if form is valid with all fields filled in" in {
      val model = formWithValidDefaults(traderBusinessName = TraderBusinessNameValid,
        traderBusinessContact = TraderBusinessContactValid,
        traderBusinessEmail = TraderBusinessEmailValid,
        traderPostcode = PostcodeValid).get
      model.businessName should equal(TraderBusinessNameValid.toUpperCase)
      model.businessContact should equal(TraderBusinessContactValid.toUpperCase)
      model.businessEmail should equal(TraderBusinessEmailValid)
      model.businessPostcode should equal(PostcodeValid)
    }
  }

  "dealerName" should {

    "reject if trader business name is blank" in {
      // IMPORTANT: The messages being returned by the form validation are overridden by the Controller
      val errors = formWithValidDefaults(traderBusinessName = "").errors
      errors should have length 3
      errors(0).key should equal(BusinessNameId)
      errors(0).message should equal("error.minLength")
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
        get.businessName should equal(TraderBusinessNameValid.toUpperCase)
    }
  }

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

  private def formWithValidDefaults(traderBusinessName: String = TraderBusinessNameValid,
                                    traderBusinessContact: String = TraderBusinessContactValid,
                                    traderBusinessEmail: String = TraderBusinessEmailValid,
                                    traderPostcode: String = PostcodeValid) = {
    injector.getInstance(classOf[SetUpBusinessDetails])
      .form.bind(
        Map(
          BusinessNameId -> traderBusinessName,
          BusinessContactId -> traderBusinessContact,
          BusinessEmailId -> traderBusinessEmail,
          BusinessPostcodeId -> traderPostcode
        )
      )
  }
}