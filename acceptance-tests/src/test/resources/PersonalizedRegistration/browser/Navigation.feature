@browser @WIP
Feature: Navigation

  Background:
    Given that I have started the PR Retention Service

  Scenario Outline: Entering a url that is before the origin page (keeper acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin    | target                        | expected         | filled       | wiped   |
    | "success" | "vehicle-lookup"              | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "confirm"                     | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "payment"                     | "vehicle-lookup" | "not filled" | "wiped" |

  Scenario Outline: Entering a url that is before the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin    | target                         | expected         | filled       | wiped   |
    | "success" | "vehicle-lookup"               | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "setup-business-details"       | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "business-choose-your-address" | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "enter-address-manually"       | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "confirm-business"             | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "confirm (business acting)"    | "vehicle-lookup" | "not filled" | "wiped" |
    | "success" | "payment"                      | "vehicle-lookup" | "not filled" | "wiped" |

  Scenario Outline: Pressing the browser's back button
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin    | expected         | wiped   |
    | "success" | "vehicle-lookup" | "wiped" |