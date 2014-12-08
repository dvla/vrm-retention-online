#Payment
Feature: Payment for the Vehicle Registration

  @HappyPath
  Scenario Outline: Valid Card Number
    Given that I have started the PR Retention Service
    And I search and confirm the vehicle to be registered
    When I enter payment details as <CardName>,<CardNumber> and <SecurityCode>
    And proceed to the payment
    Then Summary page should be displayed
  Examples:
    | CardName | CardNumber | SecurityCode |
    | ""       | ""         | ""           |

  @UnHappyPath
  Scenario Outline: InValid Card Number
    Given that I have started the PR Retention Service
    When I enter payment details as <CardName>,<CardNumber> and <SecurityCode>
    And proceed to the payment
    Then Failure Page should be displayed
  Examples:
    | CardName | CardNumber | SecurityCode |
    | ""       | ""         | ""           |
