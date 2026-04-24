package com.lottery.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.exception.BusinessException;
import com.lottery.common.response.CommonErrorCode;
import com.lottery.user.domain.entity.SysMenu;
import com.lottery.user.domain.entity.SysRole;
import com.lottery.user.domain.entity.UserAccount;
import com.lottery.user.domain.entity.UserRoleRel;
import com.lottery.user.infrastructure.mapper.SysMenuMapper;
import com.lottery.user.infrastructure.mapper.SysRoleMapper;
import com.lottery.user.infrastructure.mapper.UserAccountMapper;
import com.lottery.user.infrastructure.mapper.UserRoleRelMapper;
import com.lottery.user.model.request.LoginRequest;
import com.lottery.user.model.request.RegisterRequest;
import com.lottery.user.model.response.AuthResponse;
import com.lottery.user.model.response.MenuItemResponse;
import com.lottery.user.model.response.UserProfileResponse;
import com.lottery.user.security.AuthenticatedUser;
import com.lottery.user.service.UserAccountService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountMapper userAccountMapper;
    private final UserRoleRelMapper userRoleRelMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private final PasswordEncoder passwordEncoder;

    public UserAccountServiceImpl(UserAccountMapper userAccountMapper,
                                  UserRoleRelMapper userRoleRelMapper,
                                  SysRoleMapper sysRoleMapper,
                                  SysMenuMapper sysMenuMapper,
                                  ObjectProvider<AuthenticationManager> authenticationManagerProvider,
                                  PasswordEncoder passwordEncoder) {
        this.userAccountMapper = userAccountMapper;
        this.userRoleRelMapper = userRoleRelMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.authenticationManagerProvider = authenticationManagerProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateUniqueFields(request.username(), request.email(), normalizeMobile(request.mobile()));

        UserAccount user = new UserAccount();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setMobile(normalizeMobile(request.mobile()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setAuthSource("LOCAL");
        user.setDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.insert(user);
        bindDefaultRole(user.getId(), "LOTTERY_USER");

        return loginByUser(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManagerProvider.getObject().authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
            AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
            return loginByUser(user.getUserAccount());
        } catch (AuthenticationException exception) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED.code(), "Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse exchangeKeycloakToken(Jwt jwt) {
        String subject = jwt.getSubject();
        if (!StringUtils.hasText(subject)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST.code(), "Keycloak token is missing subject");
        }

        String username = firstNonBlank(
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                "kc_" + subject
        );
        String email = jwt.getClaimAsString("email");
        String nickname = firstNonBlank(
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("preferred_username"),
                username
        );

        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getKeycloakSubject, subject)
                .eq(UserAccount::getDeleted, false)
                .last("limit 1"));

        if (user == null && StringUtils.hasText(email)) {
            user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                    .eq(UserAccount::getEmail, email)
                    .eq(UserAccount::getDeleted, false)
                    .last("limit 1"));
        }

        if (user == null) {
            user = new UserAccount();
            user.setUsername(generateAvailableUsername(username));
            user.setNickname(nickname);
            user.setEmail(email);
            user.setEnabled(true);
            user.setAuthSource("KEYCLOAK");
            user.setKeycloakSubject(subject);
            user.setKeycloakUsername(jwt.getClaimAsString("preferred_username"));
            user.setDeleted(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userAccountMapper.insert(user);
            bindDefaultRole(user.getId(), "LOTTERY_USER");
        } else {
            user.setNickname(nickname);
            user.setEmail(email);
            user.setAuthSource("KEYCLOAK");
            user.setKeycloakSubject(subject);
            user.setKeycloakUsername(jwt.getClaimAsString("preferred_username"));
            user.setUpdatedAt(LocalDateTime.now());
            userAccountMapper.updateById(user);
        }
        bindDefaultRole(user.getId(), "IDP_USER");

        return loginByUser(user);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserProfileResponse currentUser() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED.code(), "Current user is not logged in");
        }
        UserAccount user = findById(Long.parseLong(String.valueOf(loginId)))
                .orElseThrow(() -> new BusinessException(CommonErrorCode.UNAUTHORIZED.code(), "User does not exist or has been disabled"));
        return toProfile(user);
    }

    @Override
    public Optional<AuthenticatedUser> findAuthenticatedUser(Long userId) {
        return findById(userId).map(AuthenticatedUser::new);
    }

    @Override
    public AuthenticatedUser loadAuthenticatedUserByUsername(String username) {
        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, username)
                .eq(UserAccount::getDeleted, false)
                .last("limit 1"));
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new UsernameNotFoundException("User not found");
        }
        return new AuthenticatedUser(user);
    }

    @Override
    public Optional<UserAccount> findById(Long userId) {
        return Optional.ofNullable(userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, userId)
                .eq(UserAccount::getDeleted, false)
                .eq(UserAccount::getEnabled, true)
                .last("limit 1")));
    }

    private void validateUniqueFields(String username, String email, String mobile) {
        if (exists(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, username)
                .eq(UserAccount::getDeleted, false))) {
            throw new BusinessException("Username already exists");
        }
        if (exists(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getEmail, email)
                .eq(UserAccount::getDeleted, false))) {
            throw new BusinessException("Email address is already registered");
        }
        if (StringUtils.hasText(mobile) && exists(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getMobile, mobile)
                .eq(UserAccount::getDeleted, false))) {
            throw new BusinessException("Mobile number is already registered");
        }
    }

    private boolean exists(LambdaQueryWrapper<UserAccount> wrapper) {
        return userAccountMapper.selectCount(wrapper) > 0;
    }

    private AuthResponse loginByUser(UserAccount user) {
        StpUtil.login(user.getId());
        return new AuthResponse(
                StpUtil.getTokenName(),
                StpUtil.getTokenValue(),
                user.getId(),
                toProfile(user)
        );
    }

    private UserProfileResponse toProfile(UserAccount user) {
        List<SysRole> roles = sysRoleMapper.findRolesByUserId(user.getId());
        List<SysMenu> menus = sysMenuMapper.findMenusByUserId(user.getId());
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getMobile(),
                user.getAuthSource(),
                roles.stream().map(SysRole::getRoleCode).toList(),
                menus.stream()
                        .map(menu -> new MenuItemResponse(
                                menu.getMenuCode(),
                                menu.getMenuName(),
                                menu.getPath(),
                                menu.getRouteName(),
                                menu.getComponent(),
                                menu.getIcon()
                        ))
                        .toList()
        );
    }

    private void bindDefaultRole(Long userId, String roleCode) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDeleted, false)
                .eq(SysRole::getStatus, 1)
                .last("limit 1"));
        if (role == null) {
            throw new BusinessException(CommonErrorCode.SYSTEM_ERROR.code(), "Role initialization data is missing: " + roleCode);
        }
        Long relationCount = userRoleRelMapper.selectCount(new LambdaQueryWrapper<UserRoleRel>()
                .eq(UserRoleRel::getUserId, userId)
                .eq(UserRoleRel::getRoleId, role.getId()));
        if (relationCount > 0) {
            return;
        }

        UserRoleRel relation = new UserRoleRel();
        relation.setUserId(userId);
        relation.setRoleId(role.getId());
        relation.setCreatedAt(LocalDateTime.now());
        userRoleRelMapper.insert(relation);
    }

    private String normalizeMobile(String mobile) {
        return StringUtils.hasText(mobile) ? mobile.trim() : null;
    }

    private String generateAvailableUsername(String baseUsername) {
        String normalized = baseUsername.replaceAll("[^a-zA-Z0-9_]", "_");
        String candidate = normalized;
        int suffix = 1;
        while (exists(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, candidate)
                .eq(UserAccount::getDeleted, false))) {
            candidate = normalized + "_" + suffix++;
        }
        return candidate;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
