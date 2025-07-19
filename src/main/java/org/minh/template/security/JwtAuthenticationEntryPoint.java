package org.minh.template.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.exception.AppExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.minh.template.dto.response.error.ErrorResponse;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
// class dùng để xử lý khi người dùng truy cập vào tài nguyên cần xác thực nhưng chưa đăng nhập hoặc token không hợp lệ.
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    // Khi có lỗi xác thực (ví dụ: JWT hết hạn, không hợp lệ, không tìm thấy...), phương thức commence sẽ được gọi.
    public final void commence(final HttpServletRequest request, // HttpServletRequest chứa thông tin về yêu cầu HTTP hiện tại
                               final HttpServletResponse response, // HttpServletResponse dùng để gửi phản hồi về phía client
                               final AuthenticationException e // AuthenticationException là ngoại lệ được ném ra khi có lỗi xác thực, ví dụ: token hết hạn, không hợp lệ, không tìm thấy...
    ) throws IOException {
        // Lấy các thuộc tính lỗi (expired, unsupported, invalid, illegal, notfound) từ request. Các thuộc tính này được gán ở filter khi kiểm tra JWT.
        final String expired = (String) request.getAttribute("expired");
        final String unsupported = (String) request.getAttribute("unsupported");
        final String invalid = (String) request.getAttribute("invalid");
        final String illegal = (String) request.getAttribute("illegal");
        final String notfound = (String) request.getAttribute("notfound");

        final String message; // Thông điêp lỗi sẽ được gửi về phía client.

        // Nếu có thuộc tính lỗi nào được gán trong request, thì sẽ sử dụng thông điệp tương ứng.
        if (expired != null) {
            message = expired; // Token hết hạn
        } else if (unsupported != null) {
            message = unsupported; // Token không được hỗ trợ (có thể do định dạng không đúng hoặc không phải JWT)
        } else if (invalid != null) {
            message = invalid; // Token không hợp lệ (có thể do chữ ký không đúng, hoặc token bị sửa đổi)
        } else if (illegal != null) {
            message = illegal; // Token không hợp lệ (có thể do cấu trúc không đúng, hoặc không phải JWT)
        } else if (notfound != null) {
            message = notfound; // Token không tìm thấy (có thể do không có token trong header hoặc cookie)
        } else {
            message = "Unauthorized"; // Nếu không có thuộc tính lỗi nào được gán, thì mặc định sẽ trả về thông điệp "Unauthorized"
        }

        log.error("Could not set user authentication in security context. Error: {}", message);

        // Tạo một đối tượng AppExceptionHandler và gọi hàm handleBadCredentialsException,
        // truyền vào một ngoại lệ BadCredentialsException với thông báo lỗi.
        // Hàm này trả về một đối tượng ResponseEntity<ErrorResponse> chứa thông tin lỗi chuẩn hóa.
        ResponseEntity<ErrorResponse> responseEntity = new AppExceptionHandler()
                .handleBadCredentialsException(new BadCredentialsException(message));

        // Lấy ra đối tượng ErrorResponse từ response entity.
        ErrorResponse errorResponse = responseEntity.getBody();
        if (errorResponse != null) { // Nếu có lỗi
            errorResponse.setStatusCode(HttpServletResponse.SC_UNAUTHORIZED); // Thiết lập mã trạng thái HTTP là 401 Unauthorized
        }
        // Chuyển đối tượng lỗi thành chuỗi JSON và ghi vào response trả về cho client.
        response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));

        // Thiết lập mã trạng thái HTTP là 401 Unauthorized và định dạng nội dung là JSON.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Thiết lập kiểu nội dung của phản hồi là JSON.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}