package org.minh.template.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;
import java.util.TimeZone;

import static org.minh.template.util.Constants.SECURITY_SCHEME_NAME;

@Configuration
public class AppConfig {

    @Bean
    // Bean này dùng để cấu hình ngôn ngữ và múi giờ mặc định cho ứng dụng.
    public LocaleResolver localeResolver(@Value("${app.default-locale:vi}") final String defaultLocale,
                                         @Value("${app.default-timezone:Asia/Ho_Chi_Minh}") final String defaultTimezone) {
        AcceptHeaderLocaleResolver localResolver = new AcceptHeaderLocaleResolver(); // Nếu trong request có header Accept-Language thì sẽ sử dụng ngôn ngữ đó
        localResolver.setDefaultLocale(new Locale.Builder().setLanguage(defaultLocale).build()); // Thiết lập ngôn ngữ mặc định cho ứng dụng, nếu không có header Accept-Language trong request thì sẽ sử dụng ngôn ngữ này
        TimeZone.setDefault(TimeZone.getTimeZone(defaultTimezone)); // Thiết lập múi giờ mặc định cho ứng dụng, nếu không có header Timezone trong request thì sẽ sử dụng múi giờ này

        return localResolver; // Trả về một LocaleResolver để Spring biết cách xử lý ngôn ngữ và múi giờ trong ứng dụng
    }

    // Bean này dùng để mã hóa (encoding) mật khẩu người dùng sử dụng BCryptPasswordEncoder từ Spring Security.
    @Bean
    public PasswordEncoder delegatingPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean này dùng để cấu hình OpenAPI (Swagger) cho ứng dụng, cung cấp thông tin về API như tiêu đề, mô tả, phiên bản và các thông tin bảo mật.
    @Bean
    // Định nghĩa một bean trả về đối tượng OpenAPI, dùng để cấu hình tài liệu Swagger cho API.
    public OpenAPI customOpenAPI(@Value("${spring.application.name}") final String title, // Tiêu đề của API, lấy từ cấu hình ứng dụng
                                 @Value("${spring.application.description}") final String description) { // Mô tả của API, lấy từ cấu hình ứng dụng
        return new OpenAPI() // Tạo một đối tượng OpenAPI mới để cấu hình tài liệu Swagger
                .components(new Components() // Cấu hình các thành phần của OpenAPI, bao gồm các security scheme
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme() // Thêm một security scheme với tên SECURITY_SCHEME_NAME, chỉ đơn giản là đặt tên cho scheme bảo mật này
                                .name(SECURITY_SCHEME_NAME) // Tên của security scheme, sẽ được sử dụng trong tài liệu Swagger
                                .type(SecurityScheme.Type.HTTP) // Loại của security scheme, ở đây là HTTP
                                .scheme("bearer") // Sử dụng scheme "bearer" cho bảo mật, cái này để cấu hình cho JWT
                                .bearerFormat("JWT") // Định dạng của bearer token, ở đây là JWT (JSON Web Token)
                        )
                )
                .info(new Info().title(title).version("1.0").description(description)
                        .termsOfService("https://www.mewebstudio.com")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));

        // .info(new Info()...): Thêm thông tin mô tả cho API.
        //.title(title): Tiêu đề API lấy từ cấu hình.
        //.version("1.0"): Phiên bản API.
        //.description(description): Mô tả API lấy từ cấu hình.
        //.termsOfService(...): Đường dẫn điều khoản dịch vụ.
        //.license(...): Thông tin license cho API.
    }
}
