package pl.bartek.librarymanagementapi.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestConfiguration {

  @Bean
  @ServiceConnection
  MySQLContainer mySQLContainer(){
    return new MySQLContainer("mysql:8.4.13")
            .withDatabaseName("library_management")
            .withInitScript("schema_test.sql");
  }
}
