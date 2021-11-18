package cloud.autotests.tests.demowebshop;

import cloud.autotests.config.demowebshop.App;
import cloud.autotests.helpers.AllureRestAssuredFilter;
import cloud.autotests.tests.TestBase;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Cookie;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class SomeTests extends TestBase {

    @BeforeAll
    static void configureBaseUrl() {
        RestAssured.baseURI = App.config.apiUrl();
        Configuration.baseUrl = App.config.webUrl();
    }

    @Test
    @Tag("demowebshop")
    @DisplayName("Some demowebshop (API + UI)")
    void WithCookieTest() {
        step("Get cookie by api and set it to browser", () -> {
            String authorizationCookie =
                    given()
                            .filter(AllureRestAssuredFilter.withCustomTemplates())
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .formParam("Email", App.config.userLogin())
                            .formParam("Password", App.config.userPassword())
                            .when()
                            .post("/login")
                            .then()
                            .statusCode(302)
                            .extract()
                            .cookie("NOPCOMMERCE.AUTH");

            step("Open minimal content, because cookie can be set when site is opened", () ->
                    open("/Themes/DefaultClean/Content/images/logo.png"));

            step("Set cookie to to browser", () ->
                    getWebDriver().manage().addCookie(
                            new Cookie("NOPCOMMERCE.AUTH", authorizationCookie)));
        });

        step("Add product to Wishlist", () -> {
            given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .when()
                    .body("product_attribute_5_7_1=1&addtocart_5.EnteredQuantity=1")
                    .post("http://demowebshop.tricentis.com/addproducttocart/details/5/2")
                    .then()
                    .statusCode(200)
                    .body("success", is (true))
                    .body("message", is ("The product has been added to your <a href=" +
                            "\"/wishlist\">wishlist</a>"));
        });

        step("Check Wishlist page", () ->{
            given()
                    .get("/wishlist")
                    .then()
                    .statusCode(200);});

        open("/wishlist");
        $(".product > a")
                .shouldHave(text("50's Rockabilly Polka Dot Top JR Plus Size"));
    }


}


