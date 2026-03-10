package org.j316.cellserver.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;
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

  @Test
  void oauth2LoginConfiguredReturnsEnabledFlag() {
    SecurityConfiguration configuration = new SecurityConfiguration();
    ReflectionTestUtils.setField(configuration, "oauth2Enabled", true);

    assertTrue(configuration.oauth2LoginConfigured());

    ReflectionTestUtils.setField(configuration, "oauth2Enabled", false);

    assertFalse(configuration.oauth2LoginConfigured());
  }

  @Test
  void jwtConverterMapsPermissionsAndRolesForConfiguredAudience() {
    SecurityConfiguration configuration = new SecurityConfiguration();
    ReflectionTestUtils.setField(configuration, "apiAudience", "cell-control-api");

    Jwt jwt = new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(60),
        Map.of("alg", "none"),
        Map.of(
            "aud", List.of("cell-control-api"),
            "permissions", List.of("display:write"),
            "https://j316-cell-server/roles", List.of("cell_admin")
        )
    );

    AbstractAuthenticationToken authentication = configuration.jwtAuthenticationConverter().convert(jwt);
    List<String> authorities = authentication.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .collect(Collectors.toList());

    assertTrue(authorities.contains("PERMISSION_display:write"));
    assertTrue(authorities.contains("ROLE_CELL_ADMIN"));
  }

  @Test
  void jwtConverterSkipsPermissionsWhenAudienceDoesNotMatch() {
    SecurityConfiguration configuration = new SecurityConfiguration();
    ReflectionTestUtils.setField(configuration, "apiAudience", "cell-control-api");

    Jwt jwt = new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(60),
        Map.of("alg", "none"),
        Map.of(
            "aud", List.of("another-api"),
            "permissions", List.of("display:write"),
            "https://j316-cell-server/roles", List.of("user")
        )
    );

    AbstractAuthenticationToken authentication = configuration.jwtAuthenticationConverter().convert(jwt);
    List<String> authorities = authentication.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .collect(Collectors.toList());

    assertFalse(authorities.contains("PERMISSION_display:write"));
    assertTrue(authorities.contains("ROLE_USER"));
  }
}
