package composition

import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService

final class TestReceiptEmailService extends ScalaModule with MockitoSugar {

  val stub = mock[EmailService]

  def configure() = bind[EmailService].toInstance(stub)
}