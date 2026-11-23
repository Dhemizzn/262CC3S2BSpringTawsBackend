package _CC3S2B.Spring.TawsBackend.controller;

import _CC3S2B.Spring.TawsBackend.dto.AdminPackageDTO;
import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.service.AdminPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/packages")
@RequiredArgsConstructor
public class AdminPackageController {

    private final AdminPackageService adminPackageService;

    @GetMapping
    public ResponseEntity<List<AdminPackageDTO>> getPackages(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String quotationId,
            @RequestParam(required = false) String status
    ) {
        if (authentication == null ||
                !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(401).build();
        }

        if (user.getRol() == null ||
                !Integer.valueOf(1).equals(user.getRol().getRolId())) {
            return ResponseEntity.status(403).build();
        }

        List<AdminPackageDTO> packages = adminPackageService.findPackages(
                startDate, endDate, minAmount, maxAmount, userId, quotationId, status
        );

        return ResponseEntity.ok(packages);
    }
}
