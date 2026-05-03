package ai.apps.mahefa.conf;

import ai.apps.mahefa.PojaGenerated;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;

@PojaGenerated
public class PostgresConf {

  private final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:13.9");

  void start() {
    postgres.start();
  }

  void stop() {
    postgres.stop();
  }

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgres.getUsername());
    System.setProperty("spring.datasource.password", postgres.getPassword());
  }
}
