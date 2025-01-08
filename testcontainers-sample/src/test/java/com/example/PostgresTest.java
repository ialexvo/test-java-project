package com.example;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgresTest {

    @Test
    void testLiquibaseWithPostgres() throws Exception {
        try (PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")) {
            postgresContainer.start();
            System.out.println("PostgreSQL is running at: " + postgresContainer.getJdbcUrl());
            System.out.println("Docker container logs: \n" + postgresContainer.getLogs());

            String jdbcUrl = postgresContainer.getJdbcUrl();
            String username = postgresContainer.getUsername();
            String password = postgresContainer.getPassword();

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(connection));
                Liquibase liquibase = new Liquibase("db/changelog/db.changelog.xml", new ClassLoaderResourceAccessor(), database);
                liquibase.update("");

                try (ResultSet resultSet = connection.createStatement().executeQuery(
                        "SELECT table_name FROM information_schema.tables WHERE table_name = 'users'")) {
                    assertTrue(resultSet.next(), "Table 'users' should exist in the database.");
                }
            }
        }
    }
}
