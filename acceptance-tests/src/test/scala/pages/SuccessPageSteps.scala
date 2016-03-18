package pages

import org.openqa.selenium.support.events.EventFiringWebDriver
import org.scalatest.selenium.WebBrowser.{currentUrl, Element, find, id}
import pages.vrm_retention.SuccessPage.url

class SuccessPageSteps(implicit webDriver: EventFiringWebDriver)
  extends helpers.AcceptanceTestHelper {

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `has pdf link` = {
    val element: Option[Element] = find(id("create-pdf"))
    element match {
      case Some(e) =>
        e should be ('displayed)
      case None => element should be (defined)
    }
    this
  }
}
