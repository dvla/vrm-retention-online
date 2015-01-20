package views.vrm_retention

import helpers.UiSpec
import composition.TestHarness
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

import scala.io.Source.fromInputStream

final class VersionIntegrationSpec extends UiSpec with TestHarness {

  "Version endpoint" should {
    "be declared and should include the build-details.txt from classpath" in new WebBrowserForSelenium {
      go to (WebDriverFactory.testUrl + s"$applicationContext/version")
      val t = fromInputStream(getClass.getResourceAsStream("/build-details.txt")).getLines().toSet.toList
      pageSource.lines.toSet.toList should contain allOf(t.head, t.tail.head, t.tail.tail.toSeq: _*)
    }
  }
}
