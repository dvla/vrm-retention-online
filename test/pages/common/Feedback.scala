package pages.common

import mappings.common.Feedback.FeedbackId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._

object Feedback {

  def mailto(implicit driver: WebDriver) = find(id(FeedbackId)).get
}