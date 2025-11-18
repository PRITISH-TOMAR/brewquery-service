package club.sqlhub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import club.sqlhub.Repository.UserRepository;
import club.sqlhub.constants.AppConstants;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.user.DTO.RegisterUserDTO;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import club.sqlhub.entity.user.DTO.UserLoginDTO;
import club.sqlhub.entity.utlities.UserJWTDetailsDBO;
import club.sqlhub.queries.UserQueries;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.JWTHandler;
import club.sqlhub.utils.User.UserHandler;
import lombok.RequiredArgsConstructor;

@Service
public class UserService {

    private final UserQueries queries;
    private final UserRepository repository;
    private final UserHandler userHandler;
    private final JWTHandler jwtHandler;

    public UserService(UserQueries queries, UserRepository repository, UserHandler userHandler, JWTHandler jwtHandler) {
        this.queries = queries;
        this.repository = repository;
        this.userHandler = userHandler;
        this.jwtHandler = jwtHandler;
    }

    @Transactional
    public ResponseEntity<ApiResponse<UserDetailsDTO>> registerUser(RegisterUserDTO user) {
        try {

            List<UserDetailsDBO> existUser = repository.userExists(user.getEmail(), queries.IF_USER_EXISTS);

            if (!existUser.isEmpty()) {
                return ApiResponse.call(HttpStatus.CONFLICT, MessageConstants.USER_ALREADY_EXISTS);
            }
            UserDetailsDBO newUser = userHandler.convertRegisterDtoToDbo(user);

            String salt = userHandler.generateSalt();
            String hashedPassword = userHandler.hashPassword(user.getPassword(), salt);

            newUser.setHashedPassword(hashedPassword);
            newUser.setSalt(salt);

            UserDetailsDTO createdUser = repository.addUser(newUser, queries.INSERT_USER_DETAILS,
                    queries.GET_LAST_INSERTED_USER);

            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.USER_CREATED_SUCCESSFULLY, createdUser);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<UserJWTDetailsDBO>> loginUser(UserLoginDTO user) {
        try {
            List<UserDetailsDBO> existUser = repository.userExists(user.getEmail(), queries.IF_USER_EXISTS);

            if (existUser.isEmpty()) {
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);
            }

            boolean validPassword = userHandler.validPassword(user.getPassword(), existUser.get(0).getHashedPassword(),
                    existUser.get(0).getSalt());

            if (!validPassword)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);

            String subject = existUser.get(0).getUserId().toString();

            UserJWTDetailsDBO resUser = jwtHandler.buildUserJWTDetails(subject, existUser.get(0));

            if (existUser.get(0).getStatus().equals(AppConstants.DEFAULT_STATUS)) {
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.CAN_NOT_LOGIN_NEED_TO_VERIFY_FIRST);
            }
            return ApiResponse.call(HttpStatus.OK, MessageConstants.USER_LOGIN_SUCCESSFULLY, resUser);
        } catch (Exception ex) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, ex);
        }
    }
}