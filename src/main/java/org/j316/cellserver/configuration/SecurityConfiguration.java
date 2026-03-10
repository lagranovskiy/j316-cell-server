package org.j316.cellserver.configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Value("${adapter.security.username}")
  private String username;

  @Value("${adapter.security.password}")
  private String password;

  @Value("${adapter.security.auth0.enabled:false}")
  private boolean oauth2Enabled;

  @Value("${adapter.security.auth0.audience:cell-control-api}")
  private String apiAudience;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/").hasAnyAuthority(
                "PERMISSION_display:write", "ROLE_CELL_ADMIN", "ROLE_USER")
            .anyRequest().authenticated()
        );

    if (oauth2LoginConfigured()) {
      http.oauth2Login(Customizer.withDefaults());
      http.oauth2ResourceServer(oauth ->
          oauth.jwt(jwt ->
              jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
          )
      );
    } else {
      http.httpBasic(Customizer.withDefaults());
    }

    return http.build();
  }

  boolean oauth2LoginConfigured() {
    return oauth2Enabled;
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::extractAuth0Authorities);
    return converter;
  }

  private Collection<GrantedAuthority> extractAuth0Authorities(Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    if (tokenTargetsConfiguredAudience(jwt)) {
      List<String> permissions = jwt.getClaimAsStringList("permissions");
      if (permissions != null) {
        permissions.forEach(permission ->
            authorities.add(new SimpleGrantedAuthority("PERMISSION_" + permission))
        );
      }
    }

    List<String> roles = jwt.getClaimAsStringList("https://j316-cell-server/roles");
    if (roles != null) {
      roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
    }

    return authorities;
  }

  private boolean tokenTargetsConfiguredAudience(Jwt jwt) {
    return jwt.getAudience().stream()
        .filter(Objects::nonNull)
        .anyMatch(audience -> audience.equals(apiAudience));
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
    UserDetails userDetails = User.withUsername(username)
        .password(passwordEncoder.encode(password))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(userDetails);
  }

  @Bean
  @ConditionalOnProperty(name = "adapter.security.auth0.enabled", havingValue = "true")
  public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
      ClientRegistrationRepository repo) {

    DefaultOAuth2AuthorizationRequestResolver resolver =
        new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

    resolver.setAuthorizationRequestCustomizer(builder ->
        builder.additionalParameters(params -> params.put("audience", apiAudience))
    );

    return resolver;
  }
}
