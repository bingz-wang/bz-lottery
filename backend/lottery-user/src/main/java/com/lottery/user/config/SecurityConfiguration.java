package com.lottery.user.config;

import com.lottery.user.security.SaTokenAuthenticationFilter;
import com.lottery.user.service.UserAccountService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(KeycloakSecurityProperties.class)
public class SecurityConfiguration {

    private final SecurityExceptionHandlers securityExceptionHandlers;
    private final KeycloakSecurityProperties keycloakSecurityProperties;

    public SecurityConfiguration(SecurityExceptionHandlers securityExceptionHandlers,
                                 KeycloakSecurityProperties keycloakSecurityProperties) {
        this.securityExceptionHandlers = securityExceptionHandlers;
        this.keycloakSecurityProperties = keycloakSecurityProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SaTokenAuthenticationFilter saTokenAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(securityExceptionHandlers)
                        .accessDeniedHandler(securityExceptionHandlers)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**",
                                "/api/user/ping",
                                "/api/user/auth/register",
                                "/api/user/auth/login"
                        ).permitAll()
                        .requestMatchers("/api/user/auth/keycloak/exchange").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .addFilterBefore(saTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (keycloakSecurityProperties.isEnabled() && StringUtils.hasText(keycloakSecurityProperties.getIssuerUri())) {
            http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        }

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountService userAccountService) {
        return userAccountService::loadAuthenticatedUserByUsername;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            extractRoles(jwt.getClaim("realm_access")).forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            );
            authorities.add(new SimpleGrantedAuthority("ROLE_KEYCLOAK_USER"));
            return authorities;
        });
        return converter;
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security.keycloak", name = "enabled", havingValue = "true")
    public JwtDecoder jwtDecoder() {
        JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(keycloakSecurityProperties.getIssuerUri());
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefaultWithIssuer(keycloakSecurityProperties.getIssuerUri());
        if (StringUtils.hasText(keycloakSecurityProperties.getClientId())) {
            OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<>(
                    "aud",
                    audience -> audience instanceof Iterable<?> iterable
                            ? java.util.stream.StreamSupport.stream(iterable.spliterator(), false)
                              .map(String::valueOf)
                              .anyMatch(keycloakSecurityProperties.getClientId()::equals)
                            : keycloakSecurityProperties.getClientId().equals(String.valueOf(audience))
            );
            ((org.springframework.security.oauth2.jwt.NimbusJwtDecoder) jwtDecoder)
                    .setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, audienceValidator));
        } else {
            ((org.springframework.security.oauth2.jwt.NimbusJwtDecoder) jwtDecoder).setJwtValidator(defaultValidator);
        }
        return jwtDecoder;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRoles(Object realmAccessClaim) {
        if (realmAccessClaim instanceof Map<?, ?> realmAccess) {
            Object roles = realmAccess.get("roles");
            if (roles instanceof Collection<?> collection) {
                return collection.stream().map(String::valueOf).toList();
            }
        }
        return List.of();
    }
}
