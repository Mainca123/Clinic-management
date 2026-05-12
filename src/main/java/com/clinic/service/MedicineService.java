package com.clinic.service;

import com.clinic.domain.dto.MedicineRequest;
import com.clinic.domain.dto.MedicineResponse;
import com.clinic.domain.entity.Medicine;
import com.clinic.domain.mapper.MedicineMapper;
import com.clinic.exception.ClinicException;
import com.clinic.exception.ErrorMessage;
import com.clinic.repository.MedicineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MedicineService {

    @Inject
    MedicineRepository medicineRepository;

    @Inject
    MedicineMapper medicineMapper;

    @Transactional // Bắt buộc có để lưu dữ liệu vào database
    public MedicineResponse create(MedicineRequest request) {
        // 1. Kiểm tra tên thuốc đã tồn tại chưa
        if (medicineRepository.findByName(request.getName()).isPresent()) {
            throw new ClinicException(ErrorMessage.Medicine.ALREADY_EXISTS, Response.Status.BAD_REQUEST);
        }

        // 2. Chuyển DTO sang Entity thông qua MapStruct
        Medicine medicine = medicineMapper.toEntity(request);

        // 3. Lưu vào DB (sử dụng repository thay vì persist trực tiếp)
        medicineRepository.persist(medicine);

        // 4. Trả về kết quả sau khi đã map sang Response DTO
        return medicineMapper.toResponse(medicine);
    }


    public MedicineResponse getById(Long id) {
        // 1. Tìm thuốc trong DB, nếu không có hoặc đã bị xóa thì bắn ClinicException (404)
        Medicine medicine = medicineRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ClinicException(ErrorMessage.Medicine.NOT_FOUND, Response.Status.NOT_FOUND));

        // 2. Chuyển đổi Entity sang Response DTO thông qua Mapper
        return medicineMapper.toResponse(medicine);
    }


    public List<MedicineResponse> getAll() {
        // 1. Lấy danh sách từ DB
        List<Medicine> medicines = medicineRepository.listAllActive();

        // 2. Chuyển đổi cả danh sách sang DTO thông qua Mapper
        return medicines.stream()
                .map(medicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineRequest request) {
        // 1. Tìm thuốc theo ID, nếu không thấy hoặc đã bị xóa thì báo lỗi 404
        Medicine medicine = medicineRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ClinicException(ErrorMessage.Medicine.NOT_FOUND, Response.Status.NOT_FOUND));

        // 2. Nếu người dùng muốn đổi tên thuốc, kiểm tra xem tên mới có bị trùng với thuốc khác không
        if (request.getName() != null && !request.getName().equalsIgnoreCase(medicine.getName())) {
            if (medicineRepository.findByName(request.getName()).isPresent()) {
                throw new ClinicException(ErrorMessage.Medicine.ALREADY_EXISTS, Response.Status.BAD_REQUEST);
            }
            medicine.setName(request.getName());
        }

        // 3. Cập nhật các thông tin khác
        if (request.getUnit() != null) {
            medicine.setUnit(request.getUnit());
        }

        // 4. Trả về thông tin thuốc sau khi cập nhật thông qua Mapper
        return medicineMapper.toResponse(medicine);
    }

    @Transactional
    public void delete(Long id) {
        // 1. Tìm thuốc theo ID, nếu không thấy hoặc đã xóa rồi thì báo lỗi
        Medicine medicine = medicineRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ClinicException(ErrorMessage.Medicine.NOT_FOUND, Response.Status.NOT_FOUND));

        // 2. Thực hiện xóa mềm
        medicine.setIsDeleted(true);
        medicine.setDeletedAt(java.time.Instant.now()); // Lưu lại thời điểm xóa

        // 3. Persist (với Panache, khi kết thúc method có @Transactional nó sẽ tự update)
    }
}
