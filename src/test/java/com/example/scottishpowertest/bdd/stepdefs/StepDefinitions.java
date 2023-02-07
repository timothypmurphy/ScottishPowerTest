package com.example.scottishpowertest.bdd.stepdefs;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpEntity;
import org.apache.logging.slf4j.SLF4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StepDefinitions {

    private  static  final  String  BASE_URL  =  "http://localhost:8081/api/smart/reads";
    private static final String MOCK_PATH = "src/test/resources/mocks/";
    private static final String TOO_LOW_RESPONSE = "Reading of type ELECTRIC and date 2023-02-02 is lower than the previous reading on this account, see logs for more info";
    private static final String OLD_RESPONSE = "Reading of type ELECTRIC and date 2020-02-02 is dated before the previous reading on this account, see logs for more info";
    private static final String DUPLICATE_RESPONSE = "Duplicate reading of type ELECTRIC and date 2023-02-02 found for this account, see logs for more info";
    private static final String OKAY_RESPONSE = "Readings are valid";
    RequestSpecification request;
    private  static Response response;

    private final static Logger log = LoggerFactory.getLogger(SLF4JLogger.class);

    private Map<String, String> buildMap(){
        Map<String, String> responses = new LinkedHashMap<>();
        responses.put("TOO_LOW_RESPONSE", TOO_LOW_RESPONSE);
        responses.put("OLD_RESPONSE", OLD_RESPONSE);
        responses.put("DUPLICATE_RESPONSE", DUPLICATE_RESPONSE);
        responses.put("OKAY_RESPONSE", OKAY_RESPONSE);
        return responses;
    }

    @Given("I set the GET readings api endpoint for account {int}")
    public void setGetEndpoint(int id){
        RestAssured.baseURI = BASE_URL + '/' + id;
        request = RestAssured.given();
    }

    @Given("I set the POST readings api endpoint with a {word} reading")
    public void setPostEndpoint(String typeOfReading) throws IOException {
        RestAssured.baseURI = BASE_URL;
        request = RestAssured.given();
        request.header("Content-Type",  "application/json");
        log.info(String.format("TYPE OF READING " + typeOfReading));
        request.body(Files.readAllBytes(Paths.get(MOCK_PATH + "POST_" + typeOfReading + ".json")));
    }


    @When("I send the GET request")
    public void theClientIssuesGetReadings() throws Throwable{
        response = request.request(Method.GET, "");
    }

    @When("I send the POST request")
    public void theClientIssuesPostReadings() throws Throwable{
        response = request.request(Method.POST, "");
    }

    @Then("the response is code is {int}")
    public void theResponseIs(int code) {
        log.info(String.format("Response is ", response.prettyPrint()));

        assertThat(response.getStatusCode()).isEqualTo(code);
    }

    @And("the response body is {word}")
    public void theResponseBodyIs(String typeOfResponse){
        String responseBody = response.getBody().prettyPrint();
        log.info("responseBody " + responseBody);
        log.info("TYPE OF RESPONSE " + typeOfResponse);
        String expectedResponse = buildMap().get(typeOfResponse);
        log.info("Response map " + expectedResponse);
        log.info("RESPONSE BOOL " + responseBody.equals(expectedResponse));

        assertThat(responseBody).isEqualTo(expectedResponse);
    }
}
