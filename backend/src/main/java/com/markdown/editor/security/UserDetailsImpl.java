package com.markdown.editor.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.markdown.editor.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String role;
    private String avatar;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String nickname, String role, String avatar, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.avatar = avatar;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        Long id = user.getId();
        String username = user.getUsername();
        String email = user.getEmail();
        String nickname = user.getNickname();
        String role = user.getRole();
        String avatar = user.getAvatar();
        String password = user.getPassword();
        
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new UserDetailsImpl(
            id,
            username,
            email,
            nickname,
            role,
            avatar,
            password,
            authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
