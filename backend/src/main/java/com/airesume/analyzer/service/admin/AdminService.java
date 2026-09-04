package com.airesume.analyzer.service.admin;

import com.airesume.analyzer.dto.AdminStatsDto;
import com.airesume.analyzer.dto.CandidateSearchDto;
import com.airesume.analyzer.dto.RoleChangeRequest;
import com.airesume.analyzer.dto.UserManagementDto;
import com.airesume.analyzer.entity.Recruiter;

import java.util.List;

public interface AdminService {

    AdminStatsDto getAdminStats();

    List<UserManagementDto> getAllUsers();

    UserManagementDto updateUserStatus(Long userId, String status, String adminEmail);

    UserManagementDto changeUserRole(RoleChangeRequest request, String adminEmail);

    void deleteUser(Long userId, String adminEmail);

    List<Recruiter> getAllRecruiters();

    List<CandidateSearchDto> searchCandidates(String skill, Integer minAtsScore, String adminEmail);
}
