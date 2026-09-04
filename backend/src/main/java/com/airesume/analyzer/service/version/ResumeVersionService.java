package com.airesume.analyzer.service.version;

import com.airesume.analyzer.dto.ResumeVersionDto;
import com.airesume.analyzer.entity.Resume;

import java.util.List;

public interface ResumeVersionService {

    ResumeVersionDto createNextVersion(Resume resume, String currentUserEmail);

    List<ResumeVersionDto> getVersionsForCurrentUser(String currentUserEmail);

    ResumeVersionDto getVersionById(Long id, String currentUserEmail);

    ResumeVersionDto renameVersion(Long id, String newName, String currentUserEmail);

    ResumeVersionDto restoreVersion(Long id, String currentUserEmail);

    void deleteVersion(Long id, String currentUserEmail);
}
