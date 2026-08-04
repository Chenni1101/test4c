package edu.suibe.evidence;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Ensures Flyway creates the complete schema without Hibernate's local update mode
 * silently adding missing objects. This mirrors the production ddl-auto=validate setting.
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
class SchemaMigrationValidationTest {

  @Test
  void schemaCreatedByFlywayPassesJpaValidation() {
    // Loading the application context performs the assertion.
  }
}
