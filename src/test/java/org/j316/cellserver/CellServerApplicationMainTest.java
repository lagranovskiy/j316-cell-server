package org.j316.cellserver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class CellServerApplicationMainTest {

  @Test
  void mainStartsApplication() {
    assertDoesNotThrow(() -> CellServerApplication.main(new String[] {
        "--spring.main.web-application-type=none",
        "--spring.main.banner-mode=off"
    }));
  }
}
