package br.com.erudio.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import br.com.erudio.security.jwt.JwtConfigurer;
import br.com.erudio.security.jwt.JwtTokenProvider;
import org.springframework.security.web.SecurityFilterChain;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //Esse cara vai dizer pro spring como ele deve encriptar senhas pra poder fazer a comparação com a senha que está no banco
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        Map<String, PasswordEncoder> encoders = new HashMap<>();
//        encoders.put("bcrypt", new BCryptPasswordEncoder());
//        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
//        passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
//        return passwordEncoder;
//    }

    @Bean
    PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        Pbkdf2PasswordEncoder pbkdf2PasswordEncoder = new Pbkdf2PasswordEncoder("", 8, 185000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        encoders.put("pbkdf2", pbkdf2PasswordEncoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2PasswordEncoder);
        return passwordEncoder;
    }

    @Bean
    AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .httpBasic().disable()//Aqui estamos desabilitando a autenticação Basic
                .csrf(AbstractHttpConfigurer::disable) //or (csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //Significa que a nossa sessão não vai armazenar estado, ela vai ser Stateless
                .authorizeHttpRequests(
                        //Aqui dizemos que é necessário autorização para cada requests.
                        authorizeHttpRequests -> authorizeHttpRequests
                                .requestMatchers(
                                        "/auth/signin",
                                        "/auth/refresh/**",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll() //Essas urls não precisam se autenticar
                                .requestMatchers("/api/**").authenticated() //Somente autenticado
                                .requestMatchers("/users").denyAll() //Aqui negamos as urls
                )
                .cors()
                .and()
                .apply(new JwtConfigurer(jwtTokenProvider))
                .and()
                .build();

    }

}
