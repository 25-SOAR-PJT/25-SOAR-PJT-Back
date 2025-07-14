package org.project.soar.model.user;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.dto.CustomMemberDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final CustomMemberDto customMemberDto;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    public Long getMemberId(){
        return customMemberDto.getMemberId();
    }

    @Override
    public String getPassword() {
        return customMemberDto.getMemberPassword();
    }

    @Override
    public String getUsername() {
        return customMemberDto.getMemberUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
