package com.maltsev.conference_app.security;

import com.maltsev.conference_app.exception.ForbiddenException;
import com.maltsev.conference_app.exception.NotFoundException;
import com.maltsev.conference_app.model.User;
import com.maltsev.conference_app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;

    public RoleInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RequiresRole required = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), RequiresRole.class);
        if (required == null) {
            required = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), RequiresRole.class);
        }
        if (required == null) {
            return true;
        }

        Long userId = parseUserId(request.getHeader(UserHeader.NAME));
        if (userId == null) {
            return true;
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        if (user.getRole() != required.value()) {
            throw new ForbiddenException("Role " + user.getRole() + " has no access to this resource");
        }
        return true;
    }

    private static Long parseUserId(String header) {
        if (header == null) {
            return null;
        }
        try {
            return Long.valueOf(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
