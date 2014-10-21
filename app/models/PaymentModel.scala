package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import views.vrm_retention.Payment.PaymentDetailsCacheKey

final case class PaymentModel(trxRef: Option[String] = None, var paymentStatus: Option[String] = None,
                              var maskedPAN: Option[String] = None, var authCode: Option[String] = None,
                              var merchantId: Option[String] = None, var paymentType: Option[String] = None,
                              var cardType: Option[String] = None, var totalAmountPaid: Option[Long] = None,
                              var rejectionCode: Option[String] = None)

object PaymentModel {

  def from(trxRef: String) = {

    PaymentModel(trxRef = Some(trxRef))
  }

  implicit val JsonFormat = Json.format[PaymentModel]
  implicit val Key = CacheKey[PaymentModel](PaymentDetailsCacheKey)
}