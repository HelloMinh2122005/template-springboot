package org.minh.template.config;

import lombok.RequiredArgsConstructor;
import org.minh.template.security.JwtAuthenticationEntryPoint;
import org.minh.template.security.JwtAuthenticationFilter;
import org.minh.template.util.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration  // Đánh dấu lớp này là một cấu hình Spring
@EnableWebSecurity  //  Đánh dấu lớp này là cấu hình bảo mật Web của Spring Security
@EnableMethodSecurity // Bật bảo mật phương thức, cho phép sử dụng các annotation như @PreAuthorize, @PostAuthorize, @Secured trên các phương thức trong ứng dụng
@RequiredArgsConstructor
@Profile("!mvcIt") // Chỉ kích hoạt cấu hình này khi không chạy trong profile "mvcIt", giúp tách biệt cấu hình bảo mật cho các môi trường khác nhau
public class WebSecurityConfig {
    //  Nếu có lỗi authentication, Spring Security sẽ gọi JwtAuthenticationEntryPoint để xử lý lỗi này.
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    //  Filter này sẽ kiểm tra JWT trong mỗi request, nếu hợp lệ thì sẽ xác thực người dùng.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http
                // Tắt tính năng bảo vệ CSRF (Cross-Site Request Forgery) của Spring Security.
                // Điều này phù hợp với các API REST sử dụng JWT hoặc token trong header, không dùng session/cookie để xác thực.
                // Chỉ nên tắt CSRF khi chắc chắn toàn bộ xác thực đều qua token (ví dụ JWT trong header), không dùng cookie/session.
                .csrf(AbstractHttpConfigurer::disable)

                // Thiết lập cách xử lý các ngoại lệ liên quan đến bảo mật.
                .exceptionHandling(configurer -> configurer
                        // Khi người dùng truy cập tài nguyên cần xác thực mà không hợp lệ (ví dụ: thiếu hoặc sai JWT),
                        // Spring Security sẽ chuyển hướng xử lý lỗi này đến jwtAuthenticationEntryPoint.
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // Thiết lập chính sách session là STATELESS
                // Điều này có nghĩa là ứng dụng sẽ không lưu trữ thông tin phiên làm việc của người dùng trên server,
                // mà sẽ dựa hoàn toàn vào token (ví dụ JWT) để xác thực người dùng trong mỗi yêu cầu.
                // Nói dễ hiểu hơn là mỗi request sẽ phải gửi kèm token xác thực -> phù hợp với các API RESTful với JWT.
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Tắt header X-Frame-Options trong phản hồi HTTP.
                // Mặc định, Spring Security bật header này để ngăn website của bạn bị nhúng vào iframe (bảo vệ khỏi tấn công clickjacking).
                .headers(configurer -> configurer
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )
                // Chèn jwtAuthenticationFilter vào chuỗi filter của Spring Security, đặt nó chạy trước filter UsernamePasswordAuthenticationFilter.
                // UsernamePasswordAuthenticationFilter là filter mặc định của Spring Security, xử lý đăng nhập bằng username và password.
                // đảm bảo mọi request sẽ được kiểm tra JWT trước khi đến các bước xác thực khác của Spring Security.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Cấu hình các yêu cầu HTTP, cho phép truy cập công khai đến một số đường dẫn nhất định,
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/public/**",
                                "/assets/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/ws/**"
                        ).permitAll() // Cho phép truy cập công khai đến các đường dẫn này mà không cần xác thực
                        .requestMatchers("/admin/**").hasAuthority(Constants.RoleEnum.ADMIN.name()) // Chỉ cho phép người dùng có quyền ADMIN truy cập vào các đường dẫn bắt đầu bằng /admin/
                        .anyRequest().authenticated() // Tất cả các yêu cầu khác đều cần xác thực (có token hợp lệ)
                )
                .build();
    }
}
