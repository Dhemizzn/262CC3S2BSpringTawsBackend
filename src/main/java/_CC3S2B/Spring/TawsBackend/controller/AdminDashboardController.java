
package _CC3S2B.Spring.TawsBackend.controller;

import _CC3S2B.Spring.TawsBackend.dto.DashboardStatsResponse;
import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getStatistics(
            Authentication authentication
    ) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (user.getRol() == null ||
                !Integer.valueOf(1).equals(user.getRol().getRolId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(dashboardStatsService.getStatistics());
    }
}
