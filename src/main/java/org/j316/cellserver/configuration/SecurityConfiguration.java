package org.j316.cellserver.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
            .anyRequest()
            .authenticated()
        );

    if (oauth2LoginConfigured()) {
      http.oauth2Login(withDefaults());
    } else {
      http.httpBasic(withDefaults());
    }

    return http.build();
  }

  boolean oauth2LoginConfigured() {
    return oauth2Enabled;
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
    UserDetails userDetails = User.withUsername(username)
        .password(passwordEncoder.encode(password))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(userDetails);
  }
}
