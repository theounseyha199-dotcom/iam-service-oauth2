package kh.edu.iam.infrastructure.config;

import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.user.usecase.LoadUserForAuthenticationUseCase;
import kh.edu.iam.infrastructure.security.IamUserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final LoadUserForAuthenticationUseCase load;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return IamUserPrincipal.from(load.execute(username));
        } catch (ApplicationException ex) {
            if ("not_found".equals(ex.code())) {
                throw new UsernameNotFoundException("User not found");
            }
            throw ex;
        }
    }
}
