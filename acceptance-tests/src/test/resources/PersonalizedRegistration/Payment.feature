Feature: Payment for the Vehicle Registration

  Scenario Outline: Validate Payment functionality with valid and invalid card numbers
    Given that I have started the PR Retention Service for payment
    And I search and confirm the vehicle to be registered
    When I enter payment details as <CardName>,<CardNumber> and <SecurityCode>
    And proceed to the payment
    Then following <Message> should be displayed
  Examples:
    | CardName     | CardNumber            | SecurityCode | Message              |
    | "Test Test1" | "4444333322221111"    | "123"        | "Application successful" |
    | "Test Test4" | "4012 0010 3749 0006" | "244"        | "Payment cancelled or not authorised" |
