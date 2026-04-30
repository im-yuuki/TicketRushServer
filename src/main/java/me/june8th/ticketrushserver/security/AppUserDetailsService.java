package me.june8th.ticketrushserver.security;

import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.repositories.UserAccountRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public AppUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("UserAccount " + email + " not found"));

        return new org.springframework.security.core.userdetails.User(
                userAccount.getEmail(),
                userAccount.getPasswordHash(),
                true, true, true,
                userAccount.getAccountNonLocked(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

}

