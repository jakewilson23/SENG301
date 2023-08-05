package uc.seng301.cardbattler.asg3.cucumber;

import io.cucumber.java.Before;
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
import uc.seng301.cardbattler.asg3.model.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class CreateNewBattleDeckFeature {

    private SessionFactory sessionFactory;
    private CardAccessor cardAccessor;
    private PlayerAccessor playerAccessor;
    private DeckAccessor deckAccessor;
    private CardService cardGeneratorSpy;

    private Player player;
    private Card card;
    private Deck deck;

    private String deckName;

    private Game game;
    private CommandLineInterface cli;

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

    @Given("The player {string} exists")
    public void the_player_exists(String string) {
        player = playerAccessor.createPlayer(string);
        Long playerId = playerAccessor.persistPlayer(player);
        Assertions.assertNotNull(player);
        Assertions.assertNotNull(playerId);
        Assertions.assertSame(player.getName(), string);
    }

    @Given("There is no deck named {string}")
    public void there_is_no_deck_named(String string) {
        Assertions.assertNull(deckAccessor.getDeckByName(string));
    }

    @When("I create a battle deck named {string}")
    public void i_create_a_battle_deck_named(String string) {
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
    }

    @Then("The battle deck must contain {int} cards exactly")
    public void the_battle_deck_must_contain_cards_exactly(Integer int1) {
        Assertions.assertEquals(int1, deck.getCards().size());
    }

    @Then("The battle deck contains at least {int} monsters")
    public void the_battle_deck_contains_at_least_monsters(Integer int1) {
        int monsterCount = 0;
        List<Card> testCards = deck.getCards();
        int deckSize = testCards.size();
        for (int i = 0; i < deckSize; i++) {
            if (testCards.get(i) instanceof Monster) {
                monsterCount += 1;
            }
        }
        Assertions.assertTrue(monsterCount >= int1);
    }

    @Then("The battle deck contains at least {int} spells")
    public void the_battle_deck_contains_at_least_spells(Integer int1) {
        int spellCount = 0;
        List<Card> testCards = deck.getCards();
        int deckSize = testCards.size();
        for (int i = 0; i < deckSize; i++) {
            if (testCards.get(i) instanceof Spell) {
                spellCount += 1;
            }
        }
        Assertions.assertTrue(spellCount >= int1);
    }

    @Then("The battle deck contains at least {int} traps")
    public void the_battle_deck_contains_at_least_traps(Integer int1) {
        int trapCount = 0;
        List<Card> testCards = deck.getCards();
        int deckSize = testCards.size();
        for (int i = 0; i < deckSize; i++) {
            if (testCards.get(i) instanceof Trap) {
                trapCount += 1;
            }
        }
        Assertions.assertTrue(trapCount >= int1);
    }

    @When("I create a battle deck named {string} with {int} monsters, {int} spells and {int} traps")
    public void i_create_a_battle_deck_named_with_monsters_spells_and_traps(String string, Integer int1, Integer int2, Integer int3) {
        addInputMocking("c", int1.toString(), int2.toString(), int3.toString());
        String apiResponseMonster = "{\"data\":[{\"id\":44073668,\"name\":\"Takriminos\",\"type\":\"Normal Monster\",\"frameType\":\"normal\",\"desc\":\"A member of a race of sea serpents that freely travels through the sea.\",\"atk\":1500,\"def\":1200,\"level\":4,\"race\":\"Sea Serpent\",\"attribute\":\"WATER\"}]}\n";
        String apiResponseSpell = "{\"data\":[{\"id\":34541863,\"name\":\"\\\"A\\\" Cell Breeding Device\",\"type\":\"Spell Card\",\"frameType\":\"spell\",\"desc\":\"During each of your Standby Phases, put 1 A-Counter on 1 face-up monster your opponent controls.\",\"race\":\"Continuous\",\"archetype\":\"Alien\"}]}\n";
        String apiResponseTrap = "{\"id\":68170903,\"name\":\"A Feint Plan\",\"type\":\"Trap Card\",\"frameType\":\"trap\",\"desc\":\"A player cannot attack face-down monsters during this turn.\",\"race\":\"Normal\"}\n";
        Mockito.doReturn(apiResponseMonster).when(cardGeneratorSpy).getResponseFromAPI(CardType.MONSTER);
        Mockito.doReturn(apiResponseSpell).when(cardGeneratorSpy).getResponseFromAPI(CardType.SPELL);
        Mockito.doReturn(apiResponseTrap).when(cardGeneratorSpy).getResponseFromAPI(CardType.TRAP);
        game.battleDeck("battle_deck " + player.getName() + " " + string);
        deck = deckAccessor.getDeckByName(string);
        Assertions.assertNotNull(deck);
    }

}
