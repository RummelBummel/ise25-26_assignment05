package de.seuhd.campuscoffee.acctest;

import de.seuhd.campuscoffee.api.dtos.PosDto;
import de.seuhd.campuscoffee.domain.model.CampusType;
import de.seuhd.campuscoffee.domain.model.PosType;
import de.seuhd.campuscoffee.domain.ports.PosService;
import io.cucumber.java.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static de.seuhd.campuscoffee.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the POS Cucumber tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CucumberContextConfiguration
public class CucumberPosSteps {
    static final PostgreSQLContainer<?> postgresContainer;

    static {
        // share the same testcontainers instance across all Cucumber tests
        postgresContainer = getPostgresContainer();
        postgresContainer.start();
        // testcontainers are automatically stopped when the JVM exits
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        configurePostgresContainers(registry, postgresContainer);
    }

    @Autowired
    protected PosService posService;

    @LocalServerPort
    private Integer port;

    @Before
    public void beforeEach() {
        posService.clear();
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @After
    public void afterEach() {
        posService.clear();
    }

    private List<PosDto> createdPosList;
    private PosDto updatedPos;

    /**
     * Register a Cucumber DataTable type for PosDto.
     * @param row the DataTable row to map to a PosDto object
     * @return the mapped PosDto object
     */
    @DataTableType
    @SuppressWarnings("unused")
    public PosDto toPosDto(Map<String,String> row) {
        return PosDto.builder()
                .name(row.get("name"))
                .description(row.get("description"))
                .type(PosType.valueOf(row.get("type")))
                .campus(CampusType.valueOf(row.get("campus")))
                .street(row.get("street"))
                .houseNumber(row.get("houseNumber"))
                .postalCode(Integer.parseInt(row.get("postalCode")))
                .city(row.get("city"))
                .build();
    }

    // Given -----------------------------------------------------------------------

    @Given("an empty POS list")
    public void anEmptyPosList() {
        List<PosDto> retrievedPosList = retrievePos();
        assertThat(retrievedPosList).isEmpty();
    }

    // TODO: Add Given step for new scenario
    @Given("the following POS exist:")
    public void theFollowingPosExist(List<PosDto> posList) {
        // nutzt den DataTableType oben, d.h. die Tabelle wird automatisch in List<PosDto> gemappt
        createdPosList = createPos(posList);
        // Sicherheitscheck: so viele POS wurden tatsächlich angelegt wie in der Tabelle
        assertThat(createdPosList).hasSize(posList.size());
    }


    // When -----------------------------------------------------------------------

    @When("I insert POS with the following elements")
    public void insertPosWithTheFollowingValues(List<PosDto> posList) {
        createdPosList = createPos(posList);
        assertThat(createdPosList).size().isEqualTo(posList.size());
    }

    // TODO: Add When step for new scenario
    @When("I update the POS with name {string} to have description {string}")
    public void iUpdateThePosWithNameToHaveDescription(String name, String newDescription) {
        // 1. Den ursprünglich angelegten POS mit diesem Namen in createdPosList suchen
        PosDto original = createdPosList.stream()
                .filter(pos -> pos.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No POS with name " + name + " found in createdPosList"));

        // 2. Neuen DTO-Body bauen: gleiche Daten, aber neue Beschreibung
        PosDto updateRequest = PosDto.builder()
                .id(original.id())
                .name(original.name())
                .description(newDescription)
                .type(original.type())
                .campus(original.campus())
                .street(original.street())
                .houseNumber(original.houseNumber())
                .postalCode(original.postalCode())
                .city(original.city())
                .build();

        // 3. PUT /api/pos/{id} aufrufen und Antwort wieder als PosDto einlesen
        updatedPos = RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(updateRequest)
                .when()
                .put("/api/pos/" + original.id())
                .then()
                .statusCode(200)
                .extract()
                .as(PosDto.class);
    }


    // Then -----------------------------------------------------------------------

    @Then("the POS list should contain the same elements in the same order")
    public void thePosListShouldContainTheSameElementsInTheSameOrder() {
        List<PosDto> retrievedPosList = retrievePos();
        assertThat(retrievedPosList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "updatedAt")
                .containsExactlyInAnyOrderElementsOf(createdPosList);
    }

    // TODO: Add Then step for new scenario
    @Then("the POS with name {string} should have description {string}")
    public void thePosWithNameShouldHaveDescription(String name, String expectedDescription) {
        // 1. Aktuelle POS-Liste von der API holen
        List<PosDto> allPos = retrievePos();

        // 2. Den POS mit dem gegebenen Namen finden
        PosDto found = allPos.stream()
                .filter(pos -> pos.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No POS with name " + name + " found"));

        // 3. Beschreibung prüfen
        assertThat(found.description()).isEqualTo(expectedDescription);
    }

}
