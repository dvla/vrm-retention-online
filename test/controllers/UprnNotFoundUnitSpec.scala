package controllers

import com.tzavellas.sse.guice.ScalaModule
import controllers.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout}
import utils.helpers.Config

final class UprnNotFoundUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = uprnNotFoundPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private lazy val present = {
    val request = FakeRequest()
    val uprnNotFound = testInjectorOverrideDev().getInstance(classOf[UprnNotFound])
    uprnNotFound.present(request)
  }

  private def uprnNotFoundPrototypeNotVisible = {
    testInjectorOverrideDev(new ScalaModule() {
      override def configure(): Unit = {
        val config: Config = mock[Config]
        when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
        bind[Config].toInstance(config)
      }
    }).getInstance(classOf[UprnNotFound])
  }
}