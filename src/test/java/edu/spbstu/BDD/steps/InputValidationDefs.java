package edu.spbstu.BDD.steps;

import edu.spbstu.BDD.AppContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

public class InputValidationDefs {
    private final AppContext context = new AppContext();

    public InputValidationDefs() throws IOException {
    }

    @Given("I select input option")
    public void iSelectInputOption() throws IOException, InterruptedException {
        context.waitForOutputContains("Главное меню", false);
        context.writeAndFlush("1\n");
    }

    @Given("I select manual input")
    public void iSelectManualInput() throws IOException, InterruptedException {
        context.waitForOutputContains("Выберите способ ввода данных", true);
        context.writeAndFlush("1\n");
    }

    @Given("I select JSON input")
    public void iSelectJSONInput() throws IOException, InterruptedException {
        context.waitForOutputContains("Выберите способ ввода данных", true);
        context.writeAndFlush("3\n");
    }

    @Given("I enter {string} in console")
    public void iEnterInConsole(String data) throws IOException {
        context.writeAndFlush(data + "\n");
    }

    @When("I am finishing entering data")
    public void iAmFinishingEnteringData() throws IOException {
        context.writeAndFlush("\n");
    }

    @Then("I should see no error message")
    public void iShouldSeeNoErrorMessage() {
        Assertions.assertThrows(AssertionError.class,
                () -> context.waitForOutputContains("Ошибка валидации", true));
    }

    @Then("I should see error message")
    public void iShouldSeeErrorMessage() {
        Assertions.assertDoesNotThrow(
                () -> context.waitForOutputContains("Ошибка валидации", true));
    }
}
