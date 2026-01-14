Feature: Payment Processing
  As a user
  I want to process payments via QR Code
  So that I can pay for my orders

  Scenario: Successfully create a QR Code for an order
    Given the external payment service is available
    When I request a QR Code for order "ORDER-123" with amount 150.00
    Then I should receive a successful payment response
    And the response should contain a valid QR Code data
