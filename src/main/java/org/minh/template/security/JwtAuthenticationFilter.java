package org.minh.template.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.service.user.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Profile("!mvcIT")
@Slf4j
// JwtAuthenticationFilter là một lớp filter trong Spring Security, dùng để kiểm tra và xác thực JWT cho mỗi request gửi lên server
// Lớp này kế thừa OncePerRequestFilter, nghĩa là mỗi request chỉ đi qua filter này một lần.
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    // authenticationManager là một thành phần của Spring Security, chịu trách nhiệm xác thực thông tin đăng nhập của người dùng.
    // AuthenticationManager được định nghĩa bên trong CustomAuthenticationManager
    private final AuthenticationManager authenticationManager;

    @Override
    // doFilterInternal là một phương thức trừu tượng (abstract) trong lớp OncePerRequestFilter
    // doFilterInternal là nơi cài đặt logic xử lý filter cho từng request HTTP, và chỉ chạy một lần duy nhất cho mỗi request.
    protected final void doFilterInternal(@NonNull final HttpServletRequest request, // HttpServletRequest chứa thông tin về yêu cầu HTTP hiện tại
                                          @NonNull final HttpServletResponse response, // HttpServletResponse dùng để gửi phản hồi về phía client
                                          @NonNull final FilterChain filterChain // FilterChain là chuỗi các filter sẽ được áp dụng cho request này, cho phép gọi tiếp các filter khác trong chuỗi
    ) throws ServletException, IOException {
        // Lấy token từ request thông qua jwtTokenProvider.extractJwtFromRequest(request).
        String token = jwtTokenProvider.extractJwtFromRequest(request);

        // Kiểm tra xem token có hợp lệ hay không bằng cách sử dụng jwtTokenProvider.validateToken
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token, request)) {

            // Nếu token hợp lệ, lấy user ID từ token
            String id = jwtTokenProvider.getUserIdFromToken(token);
            // Tải thông tin người dùng từ UserService bằng ID
            // (interface của Spring Security), chứa các thông tin như tên đăng nhập, mật khẩu, quyền hạn, trạng thái tài khoản,...
            // Bắt buộc có UserDetail để Spring Security có thể xác thực người dùng và thiết lập quyền truy cập.
            UserDetails user = userService.loadUserById(id);


            if (Objects.nonNull(user)) {
                // Nếu người dùng không null, tạo một đối tượng UsernamePasswordAuthenticationToken
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                /*
                * authenticationManager là một thành phần của Spring Security, chịu trách nhiệm xác thực thông tin đăng nhập của người dùng.
                    Khi gọi authenticate(auth), Spring Security sẽ kiểm tra đối tượng auth (ở đây là UsernamePasswordAuthenticationToken chứa thông tin user và quyền hạn).
                    Cơ chế hoạt động:
                     1. AuthenticationManager sẽ chuyển đối tượng auth cho các AuthenticationProvider đã cấu hình trong hệ thống.
                     2. Mỗi AuthenticationProvider sẽ kiểm tra xem có xử lý được loại xác thực này không (ở đây là xác thực bằng username/password).
                     3. Nếu đúng, nó sẽ kiểm tra thông tin user (ví dụ: tài khoản có bị khóa, có đúng quyền, có hợp lệ không...).
                     4. Nếu xác thực thành công, nó trả về một đối tượng Authentication đã được xác thực (có thông tin user, quyền hạn, trạng thái...).
                     5. Nếu thất bại, nó sẽ ném ra exception (ví dụ: BadCredentialsException).
                * */
                authenticationManager.authenticate(auth);
            }
        }

        // Tiếp tục chuỗi filter, cho phép các filter khác xử lý request này
        filterChain.doFilter(request, response);
        // Ghi log địa chỉ IP của client gửi request
        log.info(request.getRemoteAddr());
    }
}
