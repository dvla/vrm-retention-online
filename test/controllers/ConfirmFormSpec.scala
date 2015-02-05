package controllers

import helpers.UnitSpec
import models.ConfirmFormModel
import play.api.data.Form
import views.vrm_retention.Confirm.{KeeperEmailId, SupplyEmailId}
import webserviceclients.fakes.AddressLookupServiceConstants._

final class ConfirmFormSpec extends UnitSpec {

  "form" should {

    "accept when the keeper wants an email and does provide an email address" in {
      val model = buildForm().get

      model.supplyEmail should equal(supplyEmailTrue)
      model.keeperEmail should equal(KeeperEmailValid)
    }

    "accept when the keeper does not want an email and does not provide an email address" in {
      val model = buildForm(keeperEmail = keeperEmailEmpty, supplyEmail = supplyEmailFalse).get

      model.supplyEmail should equal(supplyEmailFalse)
      model.keeperEmail should equal(None)
    }

    "accept when the keeper does not want an email and does provide an email address (we just won't use the address)" in {
      val model = buildForm(supplyEmail = supplyEmailFalse).get

      model.supplyEmail should equal(supplyEmailFalse)
      model.keeperEmail should equal(KeeperEmailValid)
    }

    "reject when the supply email field has nothing selected" in {
      val errors = buildForm(supplyEmail = supplyEmailEmpty).errors
      errors.length should equal(1)
      errors(0).key should equal(SupplyEmailId)
      errors(0).message should equal("error.required")
    }

    //    "reject when the keeper wants an email and does not provide an email address" in {
    //      val errors = buildForm(keeperEmail = keeperEmailEmpty).errors
    //      errors.length should equal(1)
    //      errors(0).key should equal(KeeperEmailId)
    //      errors(0).message should equal("error.required")
    //    }
  }

  private def buildForm(keeperEmail: String = KeeperEmailValid.get,
                        supplyEmail: String = supplyEmailTrue) = {
    Form(ConfirmFormModel.Form.Mapping).bind(
      Map(
        KeeperEmailId -> keeperEmail,
        SupplyEmailId -> supplyEmail
      )
    )
  }

  private val keeperEmailEmpty = ""
  private val supplyEmailTrue = "true"
  private val supplyEmailFalse = "false"
  private val supplyEmailEmpty = ""
}
