package ru.netology.data;

import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Locale;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class UserGenerator {
    private static final Faker faker = new Faker(new Locale("en"));
    private static final Random random = new Random();

    // Спецификация для запросов
    private static final RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(9999)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    private UserGenerator() {
        // приватный конструктор
    }

    public static RegistrationDto generateActiveUser() {
        return new RegistrationDto(
                faker.name().username(),
                faker.internet().password(6, 12),
                UserStatus.ACTIVE
        );
    }

    public static RegistrationDto generateBlockedUser() {
        return new RegistrationDto(
                faker.name().username(),
                faker.internet().password(6, 12),
                UserStatus.BLOCKED
        );
    }

    public static RegistrationDto generateInvalidLoginUser() {
        return new RegistrationDto(
                "", // пустой логин
                faker.internet().password(6, 12),
                UserStatus.ACTIVE
        );
    }

    public static RegistrationDto generateInvalidPasswordUser() {
        return new RegistrationDto(
                faker.name().username(),
                "", // пустой пароль
                UserStatus.ACTIVE
        );
    }

    public static RegistrationDto generateShortPasswordUser() {
        return new RegistrationDto(
                faker.name().username(),
                faker.internet().password(1, 3), // слишком короткий пароль
                UserStatus.ACTIVE
        );
    }

    // Метод для создания пользователя через API
    public static void createUser(RegistrationDto user) {
        given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/system/users")
                .then()
                .statusCode(200);
    }
}