package composition

import _root_.webserviceclients.emailservice.EmailService
import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar

final class TestReceiptEmailService extends ScalaModule with MockitoSugar {

  val stub = mock[EmailService]

  def configure() = bind[EmailService].toInstance(stub)
}