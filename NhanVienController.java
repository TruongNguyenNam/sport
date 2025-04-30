package org.example.demaufull.Controller;


import jakarta.validation.Valid;
import org.example.demaufull.Entity.NhanVien;
import org.example.demaufull.Repository.NhanVienRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/nhan-vien")
public class NhanVienController {
    @Autowired
    NhanVienRepo repo;

    @GetMapping("/hien-thi")
    public List<NhanVien> hienThi() {
        return repo.findAll();
    }

    @GetMapping("/phan-trang")
    public List<NhanVien> phanTrang(@RequestParam(name = "page", defaultValue = "0") Integer page) {
        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);
        return repo.findAll(pageable).getContent();
    }

    @PostMapping("/them")
    public ResponseEntity<String> themNhanVien(@Valid @RequestBody NhanVien nhanVien) {
        repo.save(nhanVien);
        return ResponseEntity.ok("Thêm nhân viên thành công!");
    }

    @GetMapping("/chi-tiet/{ma}")
    public NhanVien chiTiet(@PathVariable String ma) {
        return repo.findAllByMaNhanVien(ma).get();
    }

    @GetMapping("/danh-sach/{ten}")
    public List<NhanVien> danhSach(@PathVariable("ten") String ten) {
        return repo.findNhanVienByTenAndTuoi(ten.toLowerCase());
    }

    @PutMapping("/cap-nhat")
    public ResponseEntity<String> capNhat(@RequestBody NhanVien nhanVien) {
        repo.save(nhanVien);
        return ResponseEntity.ok("Sua Thanh Cong");
    }

    @DeleteMapping("/xoa/{ma}")
    public ResponseEntity<String> xoaNhanVien(@PathVariable("ma") String ma) {
        repo.deleteByMa(ma);
        return ResponseEntity.ok("Xóa thành công nhân viên có mã: " + ma);
    }

    @GetMapping("/nu/sap-xep")
    public ResponseEntity<List<NhanVien>> danhSachGtNuTangDan() {
        List<NhanVien> danhSachBanNu = repo.danhSachGtNuTangDan();
        return ResponseEntity.ok(danhSachBanNu);
    }

    @ControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
            return ResponseEntity.badRequest().body(
                    ex.getBindingResult().getFieldErrors().stream()
                            .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage))
            );
        }
    }
    @GetMapping("/nu/gia-nhat")
    public ResponseEntity<NhanVien> findOldestFemale() {
        Optional<NhanVien> oldestFemale = repo.findAll().stream()
                .filter(nhanVien -> Boolean.TRUE.equals(nhanVien.getGioiTinh())) // Kiểm tra giới tính là true
                .min(Comparator.comparing(NhanVien::getNgaySinh));

        return oldestFemale.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null));
    }
    @GetMapping("/nhom-theo-thang")
    public Map<Integer, List<NhanVien>> groupByBirthMonthSortedByMa() {
        List<NhanVien> danhSachNhanVien = repo.findAll();
        return danhSachNhanVien.stream()
                .sorted(Comparator.comparing(NhanVien::getMaNhanVien)) // Sắp xếp theo mã
                .collect(Collectors.groupingBy(nv -> nv.getNgaySinhAsLocalDate().getMonthValue())); // Nhóm theo tháng sinh
    }

}

