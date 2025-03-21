package org.itmo.testing.lab2.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private int port = 8080;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест регистрации пользователя")
    void testUserRegistration() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(2)
    @DisplayName("Тест регистрации пользователя без userId")
    void testUserRegistrationWithMissingUserId() {
        given()
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(3)
    @DisplayName("Тест регистрации пользователя без username")
    void testUserRegistrationWithMissingUserName() {
        given()
                .queryParam("userId", "user1")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(4)
    @DisplayName("Тест регистрации пользователя с данными существующего пользователя")
    void testUserRegistrationWhenUserAlreadyExists() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("User already exists"));
    }

    @Test
    @Order(5)
    @DisplayName("Тест записи сессии")
    void testRecordSession() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(6)
    @DisplayName("Тест записи сессии без userId")
    void testRecordSessionWithMissingUserId() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(7)
    @DisplayName("Тест записи сессии без loginTime")
    void testRecordSessionWithMissingLoginTime() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(8)
    @DisplayName("Тест записи сессии без logoutTime")
    void testRecordSessionWithMissingLogoutTime() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(9)
    @DisplayName("Тест записи сессии без logoutTime")
    void testRecordSessionUserNotFound() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user100")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(startsWith("Invalid data: "));
    }

    @Test
    @Order(10)
    @DisplayName("Тест получения общего времени активности")
    void testGetTotalActivity() {
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity:"))
                .body(containsString("minutes"));
    }

    @Test
    @Order(11)
    @DisplayName("Тест получения общего времени активности без userId")
    void testGetTotalActivityWithMissingUserId() {
        given()
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(containsString("Missing userId"));
    }

    @Test
    @Order(12)
    @DisplayName("Тест получения неактивных пользователей")
    void testInactiveUsers() {
        given()
                .queryParam("days", "5")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body(equalTo("[]"));
    }

    @Test
    @Order(13)
    @DisplayName("Тест получения неактивных пользователей без days")
    void testInactiveUsersWithMissingDays() {
        given()
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Missing days parameter"));
    }

    @Test
    @Order(14)
    @DisplayName("Тест получения неактивных пользователей с некорректным days")
    void testInactiveUsersWithIncorrectDays() {
        given()
                .queryParam("days", "abc")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid number format for days"));
    }

    @Test
    @Order(15)
    @DisplayName("Тест получения активности за месяц")
    void testMonthlyActivity() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", DateTimeFormatter.ofPattern("yyyy-MM").format(LocalDate.now()))
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body(containsString(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now())));
    }
}
