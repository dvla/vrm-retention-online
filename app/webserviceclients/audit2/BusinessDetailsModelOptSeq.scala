package webserviceclients.audit2

import models.BusinessDetailsModel

object BusinessDetailsModelOptSeq {

  def from(businessDetailsModel: Option[BusinessDetailsModel]) = {
    businessDetailsModel match {
      case Some(businessDetails) =>
        val businessNameOpt = Some(("businessName", businessDetails.contact))
        val businessAddressOpt = BusinessAddressOptString.from(businessDetails).map(
          businessAddress => ("businessAddress", businessAddress))
        val businessEmailOpt = Some(("businessEmail", businessDetails.email))
        Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
      case _ => Seq.empty
    }
  }
}
