package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.mvc.Request

final class TestRefererFromHeader extends ScalaModule with MockitoSugar {

  def configure() = {
    val refererFromHeader = mock[RefererFromHeader]
    when(refererFromHeader.fetch(any[Request[_]])).thenReturn(Some("stub-referer-from-header"))
    bind[RefererFromHeader].toInstance(refererFromHeader)
  }
}