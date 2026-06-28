package ru.netology.test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.netology.data.RegistrationDto;
import ru.netology.data.UserGenerator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthTest {
    private static RequestSpecification requestSpec;

    @BeforeAll
    static void setUpAll() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(9999)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    @Test
    void shouldCreateActiveUser() {
        RegistrationDto user = UserGenerator.generateActiveUser();

        given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/system/users")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldCreateBlockedUser() {
        RegistrationDto user = UserGenerator.generateBlockedUser();

        given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/system/users")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldRewriteExistingUser() {
        // Создаем первого пользователя
        RegistrationDto user1 = UserGenerator.generateActiveUser();
        UserGenerator.createUser(user1);

        // Создаем второго пользователя с тем же логином
        RegistrationDto user2 = new RegistrationDto(
                user1.getLogin(),
                "newpassword123",
                UserGenerator.generateBlockedUser().getStatus()
        );

        // Отправляем запрос на перезапись
        given()
                .spec(requestSpec)
                .body(user2)
                .when()
                .post("/api/system/users")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldReturnErrorForEmptyLogin() {
        RegistrationDto user = UserGenerator.generateInvalidLoginUser();

        Response response = given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/system/users")
                .then()
                .statusCode(400) // или другой код ошибки, зависит от реализации
                .body(notNullValue())
                .extract().response();

        // Проверяем, что сообщение об ошибке содержит информацию о логине
        response.then().body("message", containsStringIgnoringCase("login"));
    }

    @Test
    void shouldReturnErrorForEmptyPassword() {
        RegistrationDto user = UserGenerator.generateInvalidPasswordUser();

        given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/system/users")
                .then()
                .statusCode(400)
                .body(notNullValue());
    }

    @Test
    void shouldReturnErrorForShortPassword() {
        RegistrationDto user = UserGenerator.generateShortPasswordUser();

        given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/system/users")
                .then()
                .statusCode(400)
                .body(notNullValue());
    }
}