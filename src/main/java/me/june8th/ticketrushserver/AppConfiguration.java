package me.june8th.ticketrushserver;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.june8th.ticketrushserver.security.AuthenticationFilter;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.Customizer;
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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Configuration
@EnableScheduling
@EnableCaching
@EnableWebSecurity
@EnableJpaRepositories(basePackages = "me.june8th.ticketrushserver.database")
@EnableElasticsearchRepositories(basePackages = "me.june8th.ticketrushserver.contents")
@EnableRedisRepositories(
        basePackages = "me.june8th.ticketrushserver.temp",
        enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP
)
@RequiredArgsConstructor
public class AppConfiguration implements WebMvcConfigurer {

    private final ClientIPResolver clientIPResolver;

    @Value("${app.s3.api}")
    private String s3api;

    @Value("${app.s3.access-id}")
    private String s3AccessId;

    @Value("${app.s3.secret-key}")
    private String s3SecretKey;

    @Value("${app.s3.presigned-url-cache-duration}")
    private Duration s3PresignedUrlCacheDuration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, AuthenticationFilter authenticationFilter) {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/feeds/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/admin/**").hasRole(Role.ADMINISTRATOR.name())
                        .requestMatchers("/organization/**").hasRole(Role.ORGANIZATION.name())
                        .requestMatchers("/checkin/**").hasRole(Role.STAFF.name())
                        .requestMatchers("/user/**").hasRole(Role.USER.name())
                        .requestMatchers("/purchase/**").hasRole(Role.USER.name())
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.trace("{} - Authentication failed: {}", clientIPResolver.resolve(request), authException.getMessage(), authException);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.trace("{} - Access denied: {}", clientIPResolver.resolve(request), accessDeniedException.getMessage(), accessDeniedException);
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
                .withInitialCacheConfigurations(Map.of(
                        StorageService.PRESIGNED_URL_CACHE,
                        cacheConfig.entryTtl(s3PresignedUrlCacheDuration).disableCachingNullValues()
                ))
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AWS_GLOBAL)
                .endpointOverride(URI.create(s3api))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessId, s3SecretKey)))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.AWS_GLOBAL)
                .endpointOverride(URI.create(s3api))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessId, s3SecretKey)))
                .build();
    }

}
