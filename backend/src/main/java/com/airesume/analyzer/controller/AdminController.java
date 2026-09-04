package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.AdminStatsDto;
import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.dto.RoleChangeRequest;
import com.airesume.analyzer.dto.UserManagementDto;
import com.airesume.analyzer.entity.ActivityLog;
import com.airesume.analyzer.entity.Recruiter;
import com.airesume.analyzer.service.admin.ActivityLogService;
import com.airesume.analyzer.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ActivityLogService activityLogService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminStatsDto> getAdminDashboardStats() {
        AdminStatsDto stats = adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDto>> getAllUsers() {
        List<UserManagementDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserManagementDto> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserManagementDto dto = adminService.updateUserStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("User deleted successfully")
                .success(true)
                .build());
    }

    @GetMapping("/recruiters")
    public ResponseEntity<List<Recruiter>> getAllRecruiters() {
        List<Recruiter> recruiters = adminService.getAllRecruiters();
        return ResponseEntity.ok(recruiters);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ActivityLog>> getActivityLogs() {
        List<ActivityLog> logs = activityLogService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    @PostMapping("/roles")
    public ResponseEntity<UserManagementDto> changeUserRole(
            @Valid @RequestBody RoleChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserManagementDto dto = adminService.changeUserRole(request, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }
}
