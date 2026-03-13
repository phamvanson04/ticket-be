package com.cinebee.application.service.impl;
import com.cinebee.presentation.dto.response.UserResponse;
import com.cinebee.domain.entity.User;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.application.mapper.UserMapper;
import com.cinebee.infrastructure.persistence.repository.UserRepository;
import com.cinebee.application.service.UserService;
import com.cinebee.shared.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String username) {
        User user = ServiceUtils.findObjectOrThrow(() -> userRepository.findByUsername(username), ErrorCode.USER_NOT_EXISTED);
        return UserMapper.toUserResponse(user);
    }
}

