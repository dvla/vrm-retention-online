package pages.common

import helpers.webbrowser.{Element, WebBrowserDSL}
import org.openqa.selenium.WebDriver
import views.vrm_retention.Main.BackId

object MainPanel extends WebBrowserDSL {

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get
}