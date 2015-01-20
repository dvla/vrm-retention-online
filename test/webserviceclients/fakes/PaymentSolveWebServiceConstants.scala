package webserviceclients.fakes

object PaymentSolveWebServiceConstants {

  def TransactionReferenceValid = Some("1q2w3e4r5t6y7u8i9o0p")
  def MaskedPANValid = Some("1234********1234")
  def AuthCodeValid = Some("D12345")
  def MerchantIdValid = Some("123321123")
  def PaymentTypeValid = Some("Card")
  def CardTypeValid = Some("V")
  def TotalAmountPaidValid = Some(8000L)
}
