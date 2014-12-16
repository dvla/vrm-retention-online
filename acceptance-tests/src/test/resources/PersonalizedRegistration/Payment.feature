Feature: Payment for the Vehicle Registration

  Scenario Outline: Validate Payment functionality with valid and invalid card numbers
    Given that I have started the PR Retention Service for payment
    And I search and confirm the vehicle to be registered
    When I enter payment details as <CardName>,<CardNumber> and <SecurityCode>
    And proceed to the payment
    Then following <Message> should be displayed
  Examples:
    | CardName     | CardNumber            | SecurityCode | Message              |
    | "Test Test1" | "4012 0010 3848 8884" | "242"        | "Payment Successful" |

  #Test Data requires Logo Authentication
     #| "Test Test1" | "4444 3333 2222 1111" | "123"        | "Payment Successful"                  |
     #| "Test Test2" | "4012 0010 3685 3337" | "244"        | "Payment Cancelled or Not Authorised" |

