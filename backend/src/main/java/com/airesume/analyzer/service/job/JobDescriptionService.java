package com.airesume.analyzer.service.job;

import com.airesume.analyzer.dto.JobDescriptionDto;
import com.airesume.analyzer.dto.MatchAnalysisDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface JobDescriptionService {

    JobDescriptionDto uploadJobDescription(JobDescriptionDto dto, MultipartFile file, String currentUserEmail);

    MatchAnalysisDto compareResume(Long resumeId, Long jobDescriptionId, String currentUserEmail);

    List<JobDescriptionDto> getJobDescriptionsByUser(String currentUserEmail);

    JobDescriptionDto getJobDescriptionById(Long id, String currentUserEmail);

    MatchAnalysisDto getMatchAnalysisByResume(Long resumeId, String currentUserEmail);

    void deleteJobDescription(Long id, String currentUserEmail);
}
