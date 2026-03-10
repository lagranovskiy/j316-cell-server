package org.j316.cellserver.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

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
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>();
      List<String> roles = jwt.getClaimAsStringList(
          "https://j316-cell-server/roles"
      );
      if (roles != null) {
        roles.forEach(role ->
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        );
      }
      return authorities;
    });
    return converter;
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
  public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
      ClientRegistrationRepository repo) {

    DefaultOAuth2AuthorizationRequestResolver resolver =
        new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

    resolver.setAuthorizationRequestCustomizer(builder ->
        builder.additionalParameters(params ->
            params.put("audience", "cell-control-api")
        ));

    return resolver;
  }
}
