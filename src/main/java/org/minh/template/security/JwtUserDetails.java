package org.minh.template.security;

import lombok.Data;
import org.minh.template.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data // Dùng để sinh ra các phương thức getter, setter, toString, equals, và hashCode tự động.
// UserDetails: Only defines the methods (interface).
//JwtUserDetails: Implements those methods, adds extra fields, and adapts your application's user model to Spring Security.
public final class JwtUserDetails implements UserDetails {
    private String id;

    private String email;

    private String username;

    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * JwtUserDetails constructor.
     *
     * @param id          String
     * @param email       String
     * @param password    String
     * @param authorities Collection<? extends GrantedAuthority>
     */
    private JwtUserDetails(final String id, final String email, final String password,
                           final Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Create JwtUserDetails from User.
     *
     * @param user User
     * @return JwtUserDetails
     */
    public static JwtUserDetails create(final User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(String.valueOf(user.getRole())));

        return new JwtUserDetails(user.getId().toString(), user.getEmail(), user.getPassword(), authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
