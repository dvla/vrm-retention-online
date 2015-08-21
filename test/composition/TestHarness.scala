package composition

import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.TestHarnessBase

trait TestHarness extends TestHarnessBase with DisposeGlobalCreator {

  abstract class WebBrowserForSeleniumWithPhantomJsLocal
    extends WebBrowserForSelenium(webDriver = new PhantomJSDriver(DesiredCapabilities.phantomjs))

}