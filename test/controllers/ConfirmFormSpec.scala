package controllers

import helpers.UnitSpec
import models.ConfirmFormModel
import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.mappings.OptionalToggle
import views.vrm_retention.Confirm.KeeperEmailId
import views.vrm_retention.Confirm.SupplyEmailId
import webserviceclients.fakes.AddressLookupServiceConstants._

final class ConfirmFormSpec extends UnitSpec {

  "form" should {

    "accept when the keeper wants an email and does provide an email address" in {
      val model = buildForm().get

      model.keeperEmail should equal(KeeperEmailValid)
    }

    "accept when the keeper does not want an email and does not provide an email address" in {
      val model = buildForm(keeperEmail = None).get

      model.keeperEmail should equal(None)
    }

    "reject when the keeper wants an email but does not enter an email address" in {
      val errors = buildForm(keeperEmail = Some("")).errors
      errors.length should equal(1)
      errors.head.key should equal(KeeperEmailId)
      errors.head.message should equal("error.email")
    }
  }

  private def buildForm(keeperEmail: Option[String] = KeeperEmailValid) = {
    Form(ConfirmFormModel.Form.Mapping).bind(
      Map(
      ) ++ keeperEmail.fold(Map(SupplyEmailId -> OptionalToggle.Invisible)) { email =>
        Map(SupplyEmailId -> OptionalToggle.Visible, KeeperEmailId -> email)
      }
    )
  }

  private val keeperEmailEmpty = ""
}
