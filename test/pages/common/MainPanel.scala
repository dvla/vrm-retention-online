package pages.common

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import views.vrm_retention.Main.BackId

object MainPanel {

  def back(implicit driver: WebDriver) = find(id(BackId)).get
}