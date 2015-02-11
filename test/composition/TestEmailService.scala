package composition

import com.tzavellas.sse.guice.ScalaModule
import email.RetainEmailService
import org.scalatest.mock.MockitoSugar

final class TestEmailService extends ScalaModule with MockitoSugar {

  val stub = mock[RetainEmailService]

  def configure() = bind[RetainEmailService].toInstance(stub)
}
