package pages.common

import helpers.webbrowser.{Element, WebBrowserDSL}
import mappings.common.Feedback.FeedbackId
import org.openqa.selenium.WebDriver

object Feedback extends WebBrowserDSL {

  def mailto(implicit driver: WebDriver): Element = find(id(FeedbackId)).get
}