package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.getResponseWithValidDefaults
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.invalidResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve.{PaymentSolveGetRequest, PaymentSolveWebService}

final class NotValidatedAuthorised extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[TrackingId])).
      thenReturn(Future.successful(
      new FakeResponse(
        status = OK,
        fakeJson = getResponseWithValidDefaults(response = invalidResponse)
      )
    ))
    webService
  }

  def configure() = bind[PaymentSolveWebService].toInstance(stub)
}