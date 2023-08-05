package uc.seng301.cardbattler.asg3.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import uc.seng301.cardbattler.asg3.accessor.CardAccessor;
import uc.seng301.cardbattler.asg3.accessor.DeckAccessor;
import uc.seng301.cardbattler.asg3.accessor.PlayerAccessor;
import uc.seng301.cardbattler.asg3.cards.CardService;
import uc.seng301.cardbattler.asg3.cards.CardType;
import uc.seng301.cardbattler.asg3.cli.CommandLineInterface;
import uc.seng301.cardbattler.asg3.game.Game;
import uc.seng301.cardbattler.asg3.game.GameBoard;
import uc.seng301.cardbattler.asg3.model.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

public class PlaceCardFeature {

    private SessionFactory sessionFactory;
    private CardAccessor cardAccessor;
    private PlayerAccessor playerAccessor;
    private DeckAccessor deckAccessor;
    private CardService cardGeneratorSpy;

    private Player player;

    private GameBoard gameBoard;
    private Deck deck;


    private Game game;
    private CommandLineInterface cli;

    private int testCardIndex;

    @Before
    public void setup() {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
        Configuration configuration = new Configuration();
        configuration.configure();
        sessionFactory = configuration.buildSessionFactory();
        playerAccessor = new PlayerAccessor(sessionFactory);
        deckAccessor = new DeckAccessor(sessionFactory);
        cardAccessor = new CardAccessor(sessionFactory);
        cardGeneratorSpy = Mockito.spy(new CardService());
        cli = Mockito.mock(CommandLineInterface.class);
        game = new Game(cardGeneratorSpy, cli, sessionFactory);
    }

    private void addInputMocking(String... mockedInputs) {
        Iterator<String> toMock = Arrays.asList(mockedInputs).iterator();
        Mockito.when(cli.getNextLine()).thenAnswer(i -> toMock.next());
    }

    @Given("I have a player {string} that exists")
    public void i_have_a_player_that_exists(String name) {
        player = playerAccessor.createPlayer(name);
        Long playerId = playerAccessor.persistPlayer(player);
        Assertions.assertNotNull(player);
        Assertions.assertNotNull(playerId);
        Assertions.assertSame(name, player.getName());
    }

    @And("I have a full battle deck {string}")
    public void i_have_a_full_battle_deck(String string) {
        addInputMocking("r");
        String apiResponseMonster = "{\"data\":[{\"id\":44073668,\"name\":\"Takriminos\",\"type\":\"Normal Monster\",\"frameType\":\"normal\",\"desc\":\"A member of a race of sea serpents that freely travels through the sea.\",\"atk\":1500,\"def\":1200,\"level\":4,\"race\":\"Sea Serpent\",\"attribute\":\"WATER\"}]}\n";
        String apiResponseSpell = "{\"data\":[{\"id\":34541863,\"name\":\"\\\"A\\\" Cell Breeding Device\",\"type\":\"Spell Card\",\"frameType\":\"spell\",\"desc\":\"During each of your Standby Phases, put 1 A-Counter on 1 face-up monster your opponent controls.\",\"race\":\"Continuous\",\"archetype\":\"Alien\"}]}\n";
        String apiResponseTrap = "{\"id\":68170903,\"name\":\"A Feint Plan\",\"type\":\"Trap Card\",\"frameType\":\"trap\",\"desc\":\"A player cannot attack face-down monsters during this turn.\",\"race\":\"Normal\"}\n";
        Mockito.doReturn(apiResponseMonster).when(cardGeneratorSpy).getResponseFromAPI(CardType.MONSTER);
        Mockito.doReturn(apiResponseSpell).when(cardGeneratorSpy).getResponseFromAPI(CardType.SPELL);
        Mockito.doReturn(apiResponseTrap).when(cardGeneratorSpy).getResponseFromAPI(CardType.TRAP);
        game.battleDeck("battle_deck " + player.getName() + " " + string);
        deck = deckAccessor.getDeckByName(string);
        Assertions.assertNotNull(deck);
        gameBoard = new GameBoard(deck);
        Assertions.assertNotNull(gameBoard);
    }

    @When("I draw the first five cards of my battle deck")
    public void i_draw_the_first_five_cards_of_my_battle_deck() {
        gameBoard.startGame();
        Assertions.assertNotNull(gameBoard.getHand());
        Assertions.assertTrue(gameBoard.getHand().size() > 0);
    }

    @Then("I receive the first five cards in my hand")
    public void i_receive_the_first_five_cards_in_my_hand() {
        Assertions.assertTrue(gameBoard.getHand().size() == 5);
    }

    @Given("I have one {string} Card in my hand")
    public void i_have_one_card_in_my_hand(String string) {
        gameBoard.startGame();
        Assertions.assertNotNull(gameBoard.getHand());
        Assertions.assertTrue(gameBoard.getHand().size() > 0);
        boolean cardFound = false;
        int index = 0;
        while (!(cardFound)) {
            for (int i = index; i < gameBoard.getHand().size(); i++) {
                switch (string) {
                    case "Monster" -> {
                        if (gameBoard.getHand().get(i) instanceof Monster) {
                            testCardIndex = i;
                            cardFound = true;
                        } else {
                            gameBoard.startTurn();
                            index = i;
                        }
                    }
                    case "Spell" -> {
                        if (gameBoard.getHand().get(i) instanceof Spell) {
                            testCardIndex = i;
                            cardFound = true;
                        } else {
                            gameBoard.startTurn();
                            index = i;
                        }
                    }
                    case "Trap" -> {
                        if (gameBoard.getHand().get(i) instanceof Trap) {
                            testCardIndex = i;
                            cardFound = true;
                        } else {
                            gameBoard.startTurn();
                            index = i;
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(testCardIndex);
        Assertions.assertTrue(testCardIndex >= 0);
    }

    @When("I decide I want to place my one {string} card on the board")
    public void i_decide_i_want_to_place_my_one_card_on_the_board(String string) {
        gameBoard.playCard(gameBoard.getHand().get(testCardIndex));
    }

    @Then("The {string} card is placed in the dedicated place on the board")
    public void the_card_is_placed_in_the_dedicated_place_on_the_board(String string) {
        switch (string) {
            case "Monster" -> {
                Assertions.assertTrue(gameBoard.getMonsterSlots().size() == 1);
            }
            case "Spell" -> {
                Assertions.assertTrue(gameBoard.getSpellSlots().size() == 1);
            }
            case "Trap" -> {
                Assertions.assertTrue(gameBoard.getTrapSlots().size() == 1);
            }
        }
    }

    @Given("And I have One Monster Card in my hand")
    public void and_i_have_one_monster_card_in_my_hand() {
        gameBoard.startGame();
        boolean cardFound = false;
        int index = 0;
        while (!(cardFound)) {
            for (int i = index; i < gameBoard.getHand().size(); i++) {
                if (gameBoard.getHand().get(i) instanceof Monster) {
                    testCardIndex = i;
                    cardFound = true;
                } else {
                    gameBoard.startTurn();
                    index = i;
                }
            }
        }
        Assertions.assertNotNull(testCardIndex);
        Assertions.assertTrue(testCardIndex >= 0);
    }

    @When("I decide I want to place my Monster card in {string} Position on the board")
    public void i_decide_i_want_to_place_my_monster_card_in_position_on_the_board(String string) {
        switch (string) {
            case "Attack" -> {
                ((Monster) gameBoard.getHand().get(testCardIndex)).setCardPosition(CardPosition.ATTACK);
            }
            case "Defence" -> {
                ((Monster) gameBoard.getHand().get(testCardIndex)).setCardPosition(CardPosition.DEFEND);
            }
        }
        gameBoard.playCard(gameBoard.getHand().get(testCardIndex));
        Assertions.assertTrue(gameBoard.getMonsterSlots().size() == 1);
    }

    @Then("The Monster Card starting Life is equal to its {string}")
    public void the_monster_card_starting_life_is_equal_to_its(String string) {
        switch (string) {
            case "Attack" -> {
                int testLife = gameBoard.getMonsterSlots().get(0).getLife();
                int testAttack = gameBoard.getMonsterSlots().get(0).getAttack();
                Assertions.assertTrue(testLife == testAttack);
            }
            case "Defence" -> {
                int testLife = gameBoard.getMonsterSlots().get(0).getLife();
                int testDefence = gameBoard.getMonsterSlots().get(0).getDefence();
                Assertions.assertTrue(testLife == testDefence);
            }
        }
    }

}
