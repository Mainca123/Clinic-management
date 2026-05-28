package com.clinic.service;

import com.clinic.constant.Message;
import com.clinic.domain.dto.PasswordRequest;
import com.clinic.domain.dto.UserListResponse;
import com.clinic.domain.dto.UserResponse;
import com.clinic.domain.dto.UserUpdateRequest;
import com.clinic.domain.entity.User;
import com.clinic.domain.mapper.UserMapper;
import com.clinic.exception.ErrorMessage;
import com.clinic.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;
import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper mapper;

    private User getCurrentUser(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.User.NOT_FOUND_USER));
    }

    public UserResponse getUserDetail(String identifier){
        return mapper.toUserResponse(getCurrentUser(identifier));
    }

    @Transactional
    public String updateCurrentUser(String identifier, UserUpdateRequest request) {

        User user = getCurrentUser(identifier);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        return Message.User.SUCCESS;
    }

    @Inject
    FileUploadService fileUploadService;
    @Transactional
    public String uploadAvatar(String identifier, FileUpload file) {

        User user = getCurrentUser(identifier);

        try {
            byte[] bytes = Files.readAllBytes(file.uploadedFile());

            String avatarUrl = fileUploadService.upload(bytes);

            user.setAvatarUrl(avatarUrl);

            return avatarUrl;

        } catch (Exception e) {
            throw new RuntimeException("Upload avatar failed", e);
        }
    }

    @Transactional
    public String changePassword(String identifier, PasswordRequest passwordRequest){
        if(!passwordRequest.getPassword().equals(passwordRequest.getConfirmPassword()))
            throw new RuntimeException("password.not.correct");
        User user = getCurrentUser(identifier);
        user.setPassword(BcryptUtil.bcryptHash(passwordRequest.getConfirmPassword()));
        user.setIsVerified(true);
        userRepository.persist(user);
        return "change.password.success";
    }

    // Sửa lại hàm getUsers trong file UserService.java của bạn

    public UserListResponse getUsers(Long currentUserId, String role, int page, int size) {
        List<User> users;
        long totalItems;

        // 1. Kiểm tra vai trò của người dùng đang đăng nhập
        if ("ADMIN".equals(role)) {
            // Nếu là ADMIN -> Lấy ra toàn bộ người dùng trong hệ thống
            users = userRepository.getUsers(page, size);
            totalItems = userRepository.countUsers();
        } else if ("DOCTOR".equals(role)) {
            // Nếu là DOCTOR -> Chỉ lấy danh sách bệnh nhân từng đặt lịch với bác sĩ này
            users = userRepository.getPatientsByDoctor(currentUserId, page, size);
            totalItems = userRepository.countPatientsByDoctor(currentUserId);
        } else {
            // Trường hợp User thường hoặc không hợp lệ -> Trả về danh sách rỗng hoặc quăng lỗi tùy bạn
            users = new java.util.ArrayList<>();
            totalItems = 0;
        }

        // 2. Tính toán số trang dựa trên tổng số item lấy được
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // 3. Trả về Response DTO như cũ của bạn
        return UserListResponse.builder()
                .users(mapper.toUserResponses(users))
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    // Bổ sung vào class UserService.java

    @Transactional
    public String deleteUser(Long id) {
        // 1. Tìm người dùng dựa vào ID, nếu không thấy thì ném lỗi
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.User.NOT_FOUND_USER));

        // 2. Kiểm tra nếu tài khoản này đã bị khóa từ trước rồi
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("Tài khoản này đã bị khóa trước đó.");
        }

        // 3. Thực hiện xóa mềm: Đổi trạng thái và cập nhật thời gian thực hiện
        user.setIsDeleted(true);
        user.setDeletedAt(java.time.Instant.now());

        // Nếu hệ thống quản lý có lưu thực thể bằng lệnh persist hoặc flush,
        // ở đây đối tượng được Hibernate quản lý sẽ tự sync xuống DB khi kết thúc Transaction
        userRepository.persist(user);

        return Message.User.SUCCESS; // Hoặc trả về chuỗi thông báo "Khóa người dùng thành công"
    }

    // Thêm vào file UserService.java

    public UserListResponse getPatientsForAdmin(Long doctorId, int page, int size) {
        // 1. Gọi Repository lấy danh sách bệnh nhân dựa vào Doctor ID
        List<User> patients = userRepository.getPatientsByDoctorId(doctorId, page, size);
        long totalItems = userRepository.countPatientsByDoctorId(doctorId);

        // 2. Tính toán số trang
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // 3. Map sang Response DTO và build trả về
        return UserListResponse.builder()
                .users(mapper.toUserResponses(patients))
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .build();
    }
}