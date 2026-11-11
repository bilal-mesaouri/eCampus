package com.example.eCampus.User;

import com.example.eCampus.Admin.Admin;
import com.example.eCampus.Learner.Learner;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + username));
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user instanceof Admin admin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ADMIN_LEVEL_" + admin.getPrivilegeLevel()));
        } else if (user instanceof Learner) {
            authorities.add(new SimpleGrantedAuthority("ROLE_LEARNER"));
        }
        return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getHashPassword(), authorities);
    }
}
