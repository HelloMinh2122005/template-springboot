package org.minh.template.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.entity.JwtToken;
import org.minh.template.entity.User;
import org.minh.template.exception.NotFoundException;
import org.minh.template.service.auth.JwtTokenService;
import org.minh.template.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static org.minh.template.util.Constants.TOKEN_HEADER;
import static org.minh.template.util.Constants.TOKEN_TYPE;

@Component
@Slf4j
public class JwtTokenProvider {
    private final UserService userService;

    private final String appSecret;

    @Getter
    private final Long tokenExpiresIn;

    @Getter
    private Long refreshTokenExpiresIn;

    private final Long rememberMeTokenExpiresIn;

    private final JwtTokenService jwtTokenService;

    private final HttpServletRequest httpServletRequest;

    public JwtTokenProvider(
            @Value("${app.secret}") final String appSecret,
            @Value("${app.jwt.token.expires-in}") final Long tokenExpiresIn,
            @Value("${app.jwt.refresh-token.expires-in}") final Long refreshTokenExpiresIn,
            @Value("${app.jwt.remember-me.expires-in}") final Long rememberMeTokenExpiresIn,
            final UserService userService,
            final JwtTokenService jwtTokenService,
            final HttpServletRequest httpServletRequest
    ) {
        this.userService = userService;
        this.appSecret = appSecret;
        this.tokenExpiresIn = tokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.rememberMeTokenExpiresIn = rememberMeTokenExpiresIn;
        this.jwtTokenService = jwtTokenService;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Tạo JWT token hoặc JWT refresh token mới dựa trên userId và thời gian hết hạn.
     */
    public String generateTokenByUserId(final String id, final Long expires) { // Nhận vào userId và thời gian hết hạn
        String token = Jwts.builder() // Sử dụng Jwts.builder() để tạo một JWT token mới
                .setSubject(id) // Thiết lập subject của token là userId (thường thì subject luôn là userId hoặc username, đôi khi để email cũng được)
                .setIssuedAt(new Date()) // Thiết lập thời gian phát hành token là thời điểm hiện tại
                .setExpiration(getExpireDate(expires)) // Thiết lập thời gian hết hạn của token bằng cách gọi hàm getExpireDate với tham số expires
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sử dụng hàm signWith để ký token bằng khóa bí mật (appSecret) và thuật toán HS256
                .compact(); // Gọi hàm compact() để hoàn thành việc xây dựng token và trả về chuỗi token đã được mã hóa
        log.trace("Token is added to the local cache for userID: {}, ttl: {}", id, expires);

        return token;
    }

    /**
     * Đây là phương thức để tạo JWT token cho người dùng dựa trên ID của họ.
     */
    public String generateJwt(final String id) {
        return generateTokenByUserId(id, tokenExpiresIn);
    }

    /**
     * Đây là phương thức để tạo JWT refresh token cho người dùng dựa trên ID của họ.
     */
    public String generateRefresh(final String id) {
        return generateTokenByUserId(id, refreshTokenExpiresIn);
    }

    /**
     * Hàm này nhận vào một đối tượng Authentication (chứa thông tin xác thực của user).
     * Nó gọi tiếp hàm getPrincipal của userService để lấy ra thông tin chi tiết user dưới dạng JwtUserDetails.
     * Kết quả trả về là một đối tượng JwtUserDetails (chứa thông tin user, quyền hạn, trạng thái tài khoản...).
     */
    public JwtUserDetails getPrincipal(final Authentication authentication) {
        return userService.getPrincipal(authentication);
    }

    /**
     * Hàm này dùng để lấy userId từ chuỗi JWT token.
     * Đầu tiên, gọi parseToken(token) để giải mã token và lấy ra phần body (claims).
     * Sau đó, lấy giá trị subject từ claims (thường subject chính là userId đã lưu khi tạo token).
     * Trả về userId dưới dạng chuỗi.
     */
    public String getUserIdFromToken(final String token) {
        Claims claims = parseToken(token).getBody();

        return claims.getSubject();
    }

    /**
     * Get user from token.
     *
     * @param token String
     * @return User
     */
    public User getUserFromToken(final String token) {
        try {
            return userService.findById(UUID.fromString(getUserIdFromToken(token)));
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * Boolean result of whether token is valid or not.
     *
     * @param token String token
     * @return boolean
     */
    public boolean validateToken(final String token) {
        return validateToken(token, true);
    }

    /**
     *  Kiểm tra xem JWT token có hợp lệ không.
     */
    public boolean validateToken(final String token, final boolean isHttp) {
        parseToken(token); // Giải mã và xác thực token bằng khóa bí mật. Nếu token sai định dạng hoặc bị giả mạo sẽ ném lỗi.
        try {
            JwtToken jwtToken = jwtTokenService.findByTokenOrRefreshToken(token); // để kiểm tra token có tồn tại trong hệ thống  Redis?

        } catch (NotFoundException e) {
            log.error("[JWT] Token could not found in Redis");
            return false;
        }

        return !isTokenExpired(token); // Nếu token hợp lệ và tồn tại, kiểm tra tiếp token đã hết hạn chưa
    }

    /**
     * Mục đích: Kiểm tra tính hợp lệ của JWT token và ghi chú lỗi vào request nếu token không hợp lệ.
     */
    public boolean validateToken(final String token, final HttpServletRequest httpServletRequest) {
        try {
            boolean isTokenValid = validateToken(token); // Gọi hàm validateToken(token) để kiểm tra token có hợp lệ không (chữ ký đúng, còn hạn, tồn tại trong cache/Redis).
            // Nếu token không hợp lệ, ghi log lỗi và gắn thuộc tính "notfound" vào request để báo lỗi cho phía client.
            //Nếu token hợp lệ, trả về true.
            //Nếu có exception khi kiểm tra token:
            //Nếu token không đúng chuẩn, hết hạn, sai định dạng, hoặc claims rỗng, sẽ bắt từng loại exception tương ứng.
            //Ghi log lỗi và gắn thông báo lỗi phù hợp vào request (ví dụ: "unsupported", "invalid", "expired", "illegal").
            //Nếu có lỗi, luôn trả về false.
            if (!isTokenValid) {
                log.error("[JWT] Token could not found in local cache");
                httpServletRequest.setAttribute("notfound", "Token is not found in cache");
            }
            return isTokenValid;
        } catch (UnsupportedJwtException e) {
            log.error("[JWT] Unsupported JWT token!");
            httpServletRequest.setAttribute("unsupported", "Unsupported JWT token!");
        } catch (MalformedJwtException e) {
            log.error("[JWT] Invalid JWT token!");
            httpServletRequest.setAttribute("invalid", "Invalid JWT token!");
        } catch (ExpiredJwtException e) {
            log.error("[JWT] Expired JWT token!");
            httpServletRequest.setAttribute("expired", "Expired JWT token!");
        } catch (IllegalArgumentException e) {
            log.error("[JWT] Jwt claims string is empty");
            httpServletRequest.setAttribute("illegal", "JWT claims string is empty.");
        }

        return false;
    }

    /**
     * Set jwt refresh token for remember me option.
     */
    public void setRememberMe() {
        this.refreshTokenExpiresIn = rememberMeTokenExpiresIn;
    }

    /**
     * Mục đích: Lấy ra chuỗi JWT token từ chuỗi header dạng "Bearer <token>".
     * Ví dụ:
     * Nếu bearer = "Bearer abc.def.ghi" thì hàm sẽ trả về "abc.def.ghi".
     */
    public String extractJwtFromBearerString(final String bearer) {
        // Kiểm tra chuỗi bearer có giá trị (không null, không rỗng) và bắt đầu bằng "Bearer " (giá trị của TOKEN_TYPE).
        if (StringUtils.hasText(bearer) && bearer.startsWith(String.format("%s ", TOKEN_TYPE))) {
            // Nếu đúng, cắt chuỗi token ra khỏi phần "Bearer " bằng cách lấy substring từ vị trí sau "Bearer ".
            return bearer.substring(TOKEN_TYPE.length() + 1);
        }

        // Nếu không đúng định dạng, trả về null.
        return null;
    }

    /**
     * khi cần lấy JWT token từ header của HTTP request (thường là header "Authorization"),
     * hệ thống sẽ gọi extractJwtFromRequest, và hàm này sẽ gọi tiếp extractJwtFromBearerString để lấy ra token thực sự.
     */
    public String extractJwtFromRequest(final HttpServletRequest request) {
        return extractJwtFromBearerString(request.getHeader(TOKEN_HEADER));
    }

    /**
     * Hàm này dùng để giải mã (parse) chuỗi JWT token.
     * Sử dụng thư viện io.jsonwebtoken để tạo một parser, thiết lập khóa bí mật (signing key) dùng để xác thực token.
     * Sau đó, gọi parseClaimsJws(token) để kiểm tra tính hợp lệ và lấy ra thông tin (claims) bên trong token.
     * Kết quả trả về là một đối tượng Jws<Claims> chứa toàn bộ thông tin đã giải mã từ token.
     */
    private Jws<Claims> parseToken(final String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
    }

    /**
     * Check token is expired or not.
     *
     * @param token String jwt token to get expiration date
     * @return True or False
     */
    private boolean isTokenExpired(final String token) {
        return parseToken(token).getBody().getExpiration().before(new Date());
    }

    /**
     * Get expire date.
     *
     * @return Date object
     */
    private Date getExpireDate(final Long expires) {
        return new Date(new Date().getTime() + expires);
    }

    /**
     * Get signing key.
     *
     * @return Key
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(appSecret.getBytes());
    }
}
