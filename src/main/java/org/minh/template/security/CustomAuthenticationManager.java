package org.minh.template.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.entity.User;
import org.minh.template.service.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component // Phải có @Component để Spring có thể nhận diện và quản lý bean này
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationManager implements AuthenticationManager {
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        // Lấy thông tin user từ database qua email (userService.findByEmail).
        User user = userService.findByEmail(authentication.getName());

        // Objects.nonNull(authentication.getCredentials()) kiểm tra xem giá trị credentials (thường là password)
        // trong đối tượng authentication có khác null hay không.
        if (Objects.nonNull(authentication.getCredentials())) {
            // So sánh password nhập vào với password lưu trong database.
            // Vì password lưu trong database đã được mã hóa, nên cần sử dụng PasswordEncoder để so sánh.
            boolean matches = passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword());
            if (!matches) {
                log.error("AuthenticationCredentialsNotFoundException occurred for {}", authentication.getName());
                // throw new AuthenticationCredentialsNotFoundException(messageSourceService.get("bad_credentials"));
            }
        }

        // Đến đây thì user đã xác thực thành công
        //  thông tin user và danh sách quyền (roles/authorities) sẽ được lưu trong SecurityContextHolder.
        // SecurityContextHolder là một lớp trong Spring Security, dùng để lưu trữ thông tin xác thực của người dùng hiện tại.
        // Nó cho phép truy cập thông tin xác thực của người dùng từ bất kỳ đâu trong ứng dụng,
        // bao gồm cả trong các controller, service, hoặc các lớp khác.
        // Khi request vào một endpoint, Spring Security sẽ kiểm tra các annotation như @PreAuthorize, @Secured, hoặc cấu hình trong WebSecurityConfig.
        // Hệ thống so sánh quyền của user (lấy từ SecurityContextHolder) với quyền yêu cầu của endpoint.

        // Sau đó đến bước phân quyền (authorization).

        // Ý nghĩa chung: Tạo một danh sách quyền (authorities) cho user.
        // SimpleGrantedAuthority là một lớp trong Spring Security đại diện cho một quyền (authority) của người dùng.
        // String.valueOf(user.getRole().getName())): Lấy tên của vai trò (role) của người dùng và chuyển đổi nó thành chuỗi.
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(String.valueOf(user.getRole().getName())));
        //  Lấu thông tin UserDetails từ UserService bằng email của người dùng.
        UserDetails userDetails = userService.loadUserByEmail(authentication.getName());
        // Tạo đối tượng xác thực (Authentication) mới, chứa:
        //userDetails: thông tin user.
        //user.getPassword(): password đã mã hóa.
        //authorities: danh sách quyền của user.
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                user.getPassword(), authorities);
        // Đặt đối tượng xác thực vừa tạo vào SecurityContext của Spring.
        // Điều này giúp Spring Security biết user đã đăng nhập và có quyền gì trong các request tiếp theo.
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Trả về đối tượng xác thực đã được tạo và lưu vào context.
        return auth;
    }
}
