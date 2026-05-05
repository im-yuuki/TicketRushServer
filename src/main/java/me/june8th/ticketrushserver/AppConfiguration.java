package me.june8th.ticketrushserver;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.security.AuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableScheduling
@EnableCaching
@EnableWebSecurity
@EnableMethodSecurity
@EnableRedisRepositories
@RequiredArgsConstructor
public class AppConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);

    private final ClientIPResolver clientIPResolver;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, AuthenticationFilter authenticationFilter) {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/feeds/**").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            logSecurityTrace("Authentication required", request, authException);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logSecurityTrace("Access denied", request, accessDeniedException);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public S3Client s3Client(@Value("${app.s3.url}") String url, @Value("${app.s3.access-id}") String accessId, @Value("${app.s3.secret-key}") String secretKey) {
        return S3Client.builder()
                .region(Region.AWS_GLOBAL)
                .endpointOverride(URI.create(url))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessId, secretKey)
                ))
                .build();
    }

    private void logSecurityTrace(String message, HttpServletRequest request, Exception exception) {
        String origin = request.getHeader("Origin");
        String path = request.getRequestURI();
        String clientIp = clientIPResolver.resolve(request);
        logger.trace("{} [ip={}, origin={}, path={}]", message, clientIp, origin, path, exception);
    }

}
