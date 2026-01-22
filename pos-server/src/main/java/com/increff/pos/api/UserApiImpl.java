package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class UserApiImpl implements UserApi {
    private static final Logger logger = LoggerFactory.getLogger(UserApiImpl.class);

    @Autowired
    private final UserDao userDao;

    public UserApiImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public UserPojo add(UserPojo userPojo) throws ApiException {
        logger.info("Creating user with email: {}", userPojo.getEmail());
        checkIfEmailExists(userPojo.getEmail());
        UserPojo saved = userDao.save(userPojo);
        logger.info("Created user with id: {}", saved.getId());
        return saved;
    }

    @Override
    public UserPojo get(String id) throws ApiException {
        UserPojo userPojo = userDao.findById(id).orElse(null);
        if(Objects.isNull(userPojo)) {
            throw new ApiException("User not found with id: " + id);
        }
        return userPojo;
    }

    @Override
    public List<UserPojo> getAll() {
        return userDao.findAll();
    }

    @Override
    public UserPojo getByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Page<UserPojo> getAll(int page, int size) {
        logger.info("Fetching users page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userDao.findAll(pageRequest);
    }

    private void checkIfEmailExists(String email) throws ApiException {
        UserPojo existingUserPojo = userDao.findByEmail(email);
        if(Objects.nonNull(existingUserPojo)) {
            throw new ApiException("User already exists with email: " + email);
        }
    }
} 