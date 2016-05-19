package pages.common

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.views.widgets.Prototype.FeedbackId

object Feedback {

  def mailto(implicit driver: WebDriver) = find(id(FeedbackId)).get
}
