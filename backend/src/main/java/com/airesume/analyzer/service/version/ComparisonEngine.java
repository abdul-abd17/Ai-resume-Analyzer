package com.airesume.analyzer.service.version;

import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.ResumeVersion;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ComparisonEngine {

    public static class EngineDiffResult {
        public int atsDelta;
        public int grammarDelta;
        public int jobMatchDelta;
        public List<String> addedSkills;
        public List<String> removedSkills;
        public List<String> addedProjects;
        public List<String> removedProjects;
        public List<String> addedCertifications;
        public List<String> removedCertifications;
        public String summary;
    }

    public EngineDiffResult compare(ResumeVersion oldVer, ResumeVersion newVer, ParsedResume oldParsed, ParsedResume newParsed) {
        EngineDiffResult result = new EngineDiffResult();

        // 1. Score Deltas
        int oldAts = oldVer.getAtsScore() != null ? oldVer.getAtsScore() : 0;
        int newAts = newVer.getAtsScore() != null ? newVer.getAtsScore() : 0;
        result.atsDelta = newAts - oldAts;

        int oldGrammar = oldVer.getGrammarScore() != null ? oldVer.getGrammarScore() : 0;
        int newGrammar = newVer.getGrammarScore() != null ? newVer.getGrammarScore() : 0;
        result.grammarDelta = newGrammar - oldGrammar;

        int oldMatch = oldVer.getJobMatchScore() != null ? oldVer.getJobMatchScore() : 0;
        int newMatch = newVer.getJobMatchScore() != null ? newVer.getJobMatchScore() : 0;
        result.jobMatchDelta = newMatch - oldMatch;

        // 2. DSA Set Difference for Skills (B \ A and A \ B)
        Set<String> oldSkills = parseSkillSet(oldParsed != null ? oldParsed.getSkills() : null);
        Set<String> newSkills = parseSkillSet(newParsed != null ? newParsed.getSkills() : null);

        // Added = New \ Old
        Set<String> addedSet = new HashSet<>(newSkills);
        addedSet.removeAll(oldSkills);
        result.addedSkills = new ArrayList<>(addedSet);

        // Removed = Old \ New
        Set<String> removedSet = new HashSet<>(oldSkills);
        removedSet.removeAll(newSkills);
        result.removedSkills = new ArrayList<>(removedSet);

        // 3. Project & Certification Diffing
        Set<String> oldProjects = parseSkillSet(oldParsed != null ? oldParsed.getProjects() : null);
        Set<String> newProjects = parseSkillSet(newParsed != null ? newParsed.getProjects() : null);
        Set<String> addedProjSet = new HashSet<>(newProjects);
        addedProjSet.removeAll(oldProjects);
        result.addedProjects = new ArrayList<>(addedProjSet);
        Set<String> removedProjSet = new HashSet<>(oldProjects);
        removedProjSet.removeAll(newProjects);
        result.removedProjects = new ArrayList<>(removedProjSet);

        result.addedCertifications = Collections.emptyList();
        result.removedCertifications = Collections.emptyList();

        // 4. Summary Text
        result.summary = String.format(
                "Version %d to Version %d comparison: ATS score changed by %+d points, Grammar by %+d points, and Job Alignment by %+d%%. %d skills were added and %d skills were removed.",
                oldVer.getVersionNumber(), newVer.getVersionNumber(),
                result.atsDelta, result.grammarDelta, result.jobMatchDelta,
                result.addedSkills.size(), result.removedSkills.size()
        );

        return result;
    }

    private Set<String> parseSkillSet(String skillsText) {
        if (skillsText == null || skillsText.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(skillsText.split("[,;\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
