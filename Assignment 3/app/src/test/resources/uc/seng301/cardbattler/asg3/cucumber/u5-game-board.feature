Feature: U5 As Alex, I want to place my 5 starting cards on the board so that I can start a fight.

  Scenario: AC1 - At the start of the game, I draw the first 5 cards of my battle deck.
    Given I have a player "Jane" that exists
    And I have a full battle deck "MyBattleDeck"
    When I draw the first five cards of my battle deck
    Then I receive the first five cards in my hand

  Scenario Outline: AC2 - Each card type must be placed at its dedicated place on the board.
    Given I have a player "Jane" that exists
    And I have a full battle deck "MyBattleDeck"
    And I have one <Type> Card in my hand
    When I decide I want to place my one <Type> card on the board
    Then The <Type> card is placed in the dedicated place on the board
    Examples:
      | Type      |
      | "Monster" |
      | "Spell"   |
      | "Trap"    |

  Scenario Outline: AC3 - When a monster card is placed in attack or defence mode, then its starting life is equal to its attack or
  defence respectively.
    Given I have a player "Jane" that exists
    And I have a full battle deck "MyBattleDeck"
    And And I have One Monster Card in my hand
    When I decide I want to place my Monster card in <Position> Position on the board
    Then The Monster Card starting Life is equal to its <Position>
    Examples:
      | Position  |
      | "Attack"  |
      | "Defence" |