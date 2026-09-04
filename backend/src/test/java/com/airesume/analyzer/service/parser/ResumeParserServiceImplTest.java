package com.airesume.analyzer.service.parser;

import com.airesume.analyzer.dto.ParsedResumeDto;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.repository.ParsedResumeRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ResumeParserServiceImplTest {

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ParsedResumeRepository parsedResumeRepository;

    @TempDir
    Path tempDir;

    private User ownerUser;
    private User unauthorizedUser;
    private Resume testResume;
    private File sampleResumeFile;

    @BeforeEach
    void setUp() throws Exception {
        String ownerEmail = "owner_" + UUID.randomUUID() + "@example.com";
        ownerUser = User.builder()
                .fullName("Owner User")
                .email(ownerEmail)
                .password("password123")
                .status("ACTIVE")
                .build();
        ownerUser = userRepository.save(ownerUser);

        String unauthEmail = "unauth_" + UUID.randomUUID() + "@example.com";
        unauthorizedUser = User.builder()
                .fullName("Unauthorized User")
                .email(unauthEmail)
                .password("password123")
                .status("ACTIVE")
                .build();
        unauthorizedUser = userRepository.save(unauthorizedUser);

        sampleResumeFile = tempDir.resolve("test_resume.txt").toFile();
        try (FileWriter writer = new FileWriter(sampleResumeFile)) {
            writer.write("John Doe\nEmail: john@example.com\nPhone: 123-456-7890\nSkills: Java, Spring Boot, React, SQL\nExperience: Senior Developer at TechCorp.");
        }

        testResume = Resume.builder()
                .fileName("test_resume.txt")
                .originalFileName("test_resume.txt")
                .fileType("text/plain")
                .fileSize(sampleResumeFile.length())
                .storagePath(sampleResumeFile.getAbsolutePath())
                .uploadedAt(LocalDateTime.now())
                .status("UPLOADED")
                .user(ownerUser)
                .build();
        testResume = resumeRepository.save(testResume);
    }

    @Test
    void testFirstParseCreatesParsedResume() {
        ParsedResumeDto dto = resumeParserService.parseResume(testResume.getId(), ownerUser.getEmail());

        assertNotNull(dto);
        assertEquals(testResume.getId(), dto.getResumeId());

        Optional<ParsedResume> parsedOpt = parsedResumeRepository.findByResumeId(testResume.getId());
        assertTrue(parsedOpt.isPresent());

        Resume updatedResume = resumeRepository.findById(testResume.getId()).orElseThrow();
        assertEquals("PARSED", updatedResume.getStatus());
    }

    @Test
    void testSecondParseUpdatesSameParsedResume() {
        ParsedResumeDto firstDto = resumeParserService.parseResume(testResume.getId(), ownerUser.getEmail());
        Long firstParsedId = firstDto.getId();

        ParsedResumeDto secondDto = resumeParserService.parseResume(testResume.getId(), ownerUser.getEmail());

        assertEquals(firstParsedId, secondDto.getId(), "Second parse must update the existing ParsedResume row ID");
        assertTrue(parsedResumeRepository.findByResumeId(testResume.getId()).isPresent());
        assertEquals(firstParsedId, parsedResumeRepository.findByResumeId(testResume.getId()).get().getId());
    }

    @Test
    void testRepeatedParsingDoesNotIncreaseParsedResumeRowCount() {
        boolean existedBefore = parsedResumeRepository.findByResumeId(testResume.getId()).isPresent();
        assertFalse(existedBefore);

        for (int i = 0; i < 5; i++) {
            resumeParserService.parseResume(testResume.getId(), ownerUser.getEmail());
        }

        boolean existsAfter = parsedResumeRepository.findByResumeId(testResume.getId()).isPresent();
        assertTrue(existsAfter);
    }

    @Test
    void testConcurrentParsingOfSameResumeDoesNotProduceUnhandledUniqueConstraintError() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<ParsedResumeDto>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> resumeParserService.parseResume(testResume.getId(), ownerUser.getEmail()));
        }

        List<Future<ParsedResumeDto>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        for (Future<ParsedResumeDto> future : futures) {
            ParsedResumeDto result = assertDoesNotThrow(() -> future.get(), "Concurrent parsing must not throw unique constraint exceptions");
            assertNotNull(result);
            assertEquals(testResume.getId(), result.getResumeId());
        }

        Optional<ParsedResume> parsedOpt = parsedResumeRepository.findByResumeId(testResume.getId());
        assertTrue(parsedOpt.isPresent());
    }

    @Test
    void testUnauthorizedUserCannotParseAnotherUserResume() {
        assertThrows(UnauthorizedAccessException.class, () ->
                resumeParserService.parseResume(testResume.getId(), unauthorizedUser.getEmail())
        );
    }
}
