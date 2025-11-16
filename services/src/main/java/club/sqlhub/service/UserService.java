package club.sqlhub.service;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.user.UserDetailsDBO;
import club.sqlhub.queries.UserQueries;
import club.sqlhub.utils.APiResponse.ApiResponse;

@Service
public class UserService {

    private final JdbcTemplate jdbc;
    private final UserQueries queries;

    public UserService(JdbcTemplate jdbc, UserQueries queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    @Transactional
    public ResponseEntity<ApiResponse<UserDetailsDBO>> registerUser(UserDetailsDBO user) {
        try {

            String userExists = queries.IF_USER_EXISTS;

            List<UserDetailsDBO> existUser = jdbc.query(userExists,
                    new BeanPropertyRowMapper<>(UserDetailsDBO.class), user.getEmail());

            if (!existUser.isEmpty()) {
                return ApiResponse.call(HttpStatus.CONFLICT, MessageConstants.USER_ALREADY_EXISTS);
            }

            String query = queries.INSERT_USER_DETAILS;

            jdbc.update(query, user.getFirstName(), user.getLastName(), user.getEmail(), user.getRoleId(),
                    user.getPhoneNumber(), user.getCountryCode(), user.getProfilePictureUrl());

            String lastInsertedUser = queries.GET_LAST_INSERTED_USER;

            UserDetailsDBO createdUser = jdbc.queryForObject(lastInsertedUser,
                    new BeanPropertyRowMapper<>(UserDetailsDBO.class));

            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.USER_CREATED_SUCCESSFULLY, createdUser);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }
}