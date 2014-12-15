package pages.common

import org.openqa.selenium.{By, WebDriver}

object ErrorPanel {

  def numberOfErrors(implicit driver: WebDriver): Int =
    driver.findElement(By.cssSelector(".validation-summary")).findElements(By.tagName("li")).size

  def text(implicit driver: WebDriver): String =
    driver.findElement(By.cssSelector(".validation-summary")).getText
}