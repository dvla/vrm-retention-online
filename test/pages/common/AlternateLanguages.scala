package pages.common

import mappings.common.AlternateLanguages.{CyId, EnId}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._

object AlternateLanguages {

  def cymraeg(implicit driver: WebDriver) = find(id(CyId)).get

  def english(implicit driver: WebDriver) = find(id(EnId)).get

  def hasCymraeg(implicit driver: WebDriver): Boolean = find(id(CyId)).isDefined

  def hasEnglish(implicit driver: WebDriver): Boolean = find(id(EnId)).isDefined
}