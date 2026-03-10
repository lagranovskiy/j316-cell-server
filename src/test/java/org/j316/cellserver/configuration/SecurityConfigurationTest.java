package org.j316.cellserver.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.util.ReflectionTestUtils;

class SecurityConfigurationTest {

  @Test
  void createsPasswordEncoder() {
    SecurityConfiguration configuration = new SecurityConfiguration();

    PasswordEncoder encoder = configuration.passwordEncoder();

    assertNotNull(encoder);
    assertTrue(encoder.matches("secret", encoder.encode("secret")));
  }

  @Test
  void createsInMemoryUserWithConfiguredCredentials() {
    SecurityConfiguration configuration = new SecurityConfiguration();
    ReflectionTestUtils.setField(configuration, "username", "cell");
    ReflectionTestUtils.setField(configuration, "password", "pass123");

    PasswordEncoder passwordEncoder = configuration.passwordEncoder();
    InMemoryUserDetailsManager userDetailsManager = configuration.inMemoryUserDetailsManager(passwordEncoder);
    UserDetails user = userDetailsManager.loadUserByUsername("cell");

    assertNotNull(user);
    assertTrue(passwordEncoder.matches("pass123", user.getPassword()));
    assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }
}
