package composition

import com.tzavellas.sse.guice.ScalaModule
import email.EmailService
import org.scalatest.mock.MockitoSugar

final class TestEmailService extends ScalaModule with MockitoSugar {

  val stub = mock[EmailService]

  def configure() = bind[EmailService].toInstance(stub)
}
