package me.june8th.ticketrushserver.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountDetailsService implements UserDetailsService {

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // if (!username.startsWith(PREFIX)) {
        //     throw new UsernameNotFoundException("Invalid username format");
        // }
        // try {
        //     Long id = Long.parseLong(username.substring(PREFIX.length()));
        //     Account account = accountRepository.findById(id).orElseThrow(
        //             () -> new UsernameNotFoundException("Account not found")
        //     );
        //     return UserAccount.withUsername(username)
        //             .password("")
        //             .roles()
        //             .accountLocked(account.getLocked())
        //             .build();
        // } catch (NumberFormatException e) {
        //     throw new UsernameNotFoundException("Invalid username format");
        // }
        throw new UsernameNotFoundException("Not implemented");
    }


}
