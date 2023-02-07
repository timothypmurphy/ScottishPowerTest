Feature: meter readings service
  Scenario Outline: client requests meter readings for an account
    Given I set the GET readings api endpoint for account <account number>
    When I send the GET request
    Then the response is <response code>
    Examples:
      | account number | response code |
      | 1              | 200           |
      | 3              | 404           |

  Scenario Outline: client sends a meter reading
    Given I set the POST readings api endpoint with a <reading type> reading
    When I send the POST request
    Then the response is code is <response code>
    And the response body is <response body>
    Examples:
      | reading type | response code | response body      |
      | OKAY         | 200           | OKAY_RESPONSE      |
      | DUPLICATE    | 409           | DUPLICATE_RESPONSE |
      | OLD          | 409           | OLD_RESPONSE       |
      | TOO_LOW      | 409           | TOO_LOW_RESPONSE   |