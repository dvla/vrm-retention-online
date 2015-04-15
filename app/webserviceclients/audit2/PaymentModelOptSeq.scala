package webserviceclients.audit2

import models.PaymentModel

object PaymentModelOptSeq {

  def from(paymentModelOpt: Option[PaymentModel]) = {
    paymentModelOpt match {
      case Some(paymentModel) =>
        val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
        val paymentStatusOpt = paymentModel.paymentStatus.map(paymentStatus => ("paymentStatus", paymentStatus))
        val paymentMaskedPanOpt = Some(("paymentMaskedPan", "****************")) //paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
      val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
        val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
        val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
        val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
        val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(
          totalAmountPaid => ("paymentTotalAmountPaid", totalAmountPaid / 100.0))
        Seq(paymentTrxRefOpt, paymentStatusOpt, paymentMaskedPanOpt, paymentAuthCodeOpt, paymentMerchantIdOpt,
          paymentTypeOpt, paymentCardTypeOpt, paymentTotalAmountPaidOpt)
      case _ => Seq.empty
    }
  }
}
