package com.officebuddy.jobswitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officebuddy.document.Document;
import com.officebuddy.document.DocumentRepository;
import com.officebuddy.document.DocumentType;
import com.officebuddy.jobswitch.dto.JobSwitchDownloadDetailsDto;
import com.officebuddy.jobswitch.dto.JobSwitchGenerateRequest;
import com.officebuddy.jobswitch.dto.JobSwitchPackDto;
import com.officebuddy.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSwitchService {

    private final JobSwitchRepository jobSwitchRepository;
    private final JobSwitchPackDownloadDetailsRepository downloadDetailsRepository;
    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobSwitchPackDto generatePack(UUID userId) {
        return generatePack(userId, null);
    }

    public JobSwitchPackDto generatePack(UUID userId, JobSwitchGenerateRequest req) {
        var documents = documentRepository.findByUserIdOrderByUploadedAtDesc(userId);

        boolean custom = req != null && req.getIncludeCounts() != null && !req.getIncludeCounts().isEmpty();

        List<Document> experienceDocs;
        List<Document> payslips;
        List<Document> joiningDocs;
        List<Document> incrementDocs;
        List<Document> offerDocs;

        if (!custom) {
            experienceDocs = documents.stream()
                    .filter(d -> d.getType() == DocumentType.CERTIFICATE || d.getType() == DocumentType.RELIEVING_LETTER)
                    .toList();
            payslips = documents.stream().filter(d -> d.getType() == DocumentType.PAYSLIP).limit(3).toList();
            joiningDocs = documents.stream().filter(d -> d.getType() == DocumentType.JOINING_LETTER).toList();
            incrementDocs = documents.stream().filter(d -> d.getType() == DocumentType.INCREMENT_LETTER).toList();
            offerDocs = documents.stream().filter(d -> d.getType() == DocumentType.OFFER_LETTER).toList();
        } else {
            var map = req.getIncludeCounts();
            experienceDocs = getLimited(documents, Set.of(DocumentType.CERTIFICATE, DocumentType.RELIEVING_LETTER), map);
            payslips = getLimited(documents, Set.of(DocumentType.PAYSLIP), map);
            joiningDocs = getLimited(documents, Set.of(DocumentType.JOINING_LETTER), map);
            incrementDocs = getLimited(documents, Set.of(DocumentType.INCREMENT_LETTER), map);
            offerDocs = getLimited(documents, Set.of(DocumentType.OFFER_LETTER), map);
        }

        String selectedJson = null;
        try {
            if (custom) selectedJson = objectMapper.writeValueAsString(req.getIncludeCounts());
        } catch (Exception e) {
            log.warn("Failed to serialize selectedTypes", e);
        }

        try {
            var zipBytes = createZipBundle(experienceDocs, payslips, joiningDocs, incrementDocs, offerDocs);

            var fileName = "job-switch-pack-" + UUID.randomUUID() + ".zip";
            var result = storageService.uploadBytes(fileName, zipBytes, "application/zip");

            var pack = JobSwitchPack.builder()
                    .userId(userId)
                    .status("READY")
                    .bundleKey(result.getKey())
                    .selectedTypes(selectedJson)
                    .active(true)
                    .downloadCount(0)
                    .isPaid(false)
                    .build();

            jobSwitchRepository.save(pack);

            return JobSwitchPackDto.builder()
                    .id(pack.getId().toString())
                    .status(pack.getStatus())
                    .downloadUrl(result.getUrl())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate job switch pack", e);
        }
    }

    private List<Document> getLimited(List<Document> all, Set<DocumentType> types, Map<DocumentType, Integer> map) {
        // Find count: if any of the types is in map, use that count; for Experience folder (2 types) take max of both counts if both present
        Integer count = null;
        for (var t : types) {
            if (map.containsKey(t)) {
                int v = map.get(t);
                if (v <= 0) return List.of(); // 0 = off
                if (count == null || v > count) count = v;
            }
        }
        if (count == null) return List.of(); // folder not selected
        var filtered = all.stream().filter(d -> types.contains(d.getType())).collect(Collectors.toList());
        if (count >= filtered.size()) return filtered;
        return filtered.subList(0, count);
    }

    public JobSwitchPackDto getPackStatus(UUID userId) {
        var pack = jobSwitchRepository.findTopByUserIdOrderByGeneratedAtDesc(userId);
        if (pack.isEmpty()) {
            return JobSwitchPackDto.builder().status("NOT_FOUND").build();
        }
        var p = pack.get();
        if (p.getDeletedAt() != null) {
            return JobSwitchPackDto.builder().status("NOT_FOUND").build();
        }
        return JobSwitchPackDto.builder()
                .id(p.getId().toString())
                .status(p.getStatus())
                .downloadUrl(storageService.getPresignedUrl(p.getBundleKey()))
                .build();
    }

    public JobSwitchPackDto getPackStatusById(UUID userId, UUID packId) {
        var p = jobSwitchRepository.findById(packId).orElse(null);
        if (p == null || !p.getUserId().equals(userId) || p.getDeletedAt() != null) {
            return JobSwitchPackDto.builder().status("NOT_FOUND").build();
        }
        return JobSwitchPackDto.builder()
                .id(p.getId().toString())
                .status(p.getStatus())
                .downloadUrl(storageService.getPresignedUrl(p.getBundleKey()))
                .build();
    }

    // Download tracking — row-wise, paid feasibility
    public JobSwitchPackDto recordDownload(UUID userId, UUID packId, String ip, String userAgent) {
        var pack = jobSwitchRepository.findById(packId)
                .orElseThrow(() -> new RuntimeException("Pack not found"));
        if (!pack.getUserId().equals(userId)) throw new RuntimeException("Access denied");
        if (pack.getDeletedAt() != null || Boolean.FALSE.equals(pack.getActive())) throw new RuntimeException("Pack not active");

        // Paid gate: 3 free downloads, then require isPaid
        long userDownloadCount = downloadDetailsRepository.countByUserIdAndDeletedAtIsNull(userId);
        if (userDownloadCount >= 3 && !Boolean.TRUE.equals(pack.getIsPaid())) {
            throw new RuntimeException("Free download limit reached. Please upgrade to paid plan.");
        }

        String snapshot = pack.getSelectedTypes();
        var log = JobSwitchPackDownloadDetails.builder()
                .userId(userId)
                .packId(packId)
                .downloadCount(1)
                .active(true)
                .ipAddress(ip)
                .userAgent(userAgent)
                .selectedTypesSnapshot(snapshot)
                .build();
        downloadDetailsRepository.save(log);

        // Increment pack downloadCount
        pack.setDownloadCount((pack.getDownloadCount() == null ? 0 : pack.getDownloadCount()) + 1);
        pack.setUpdatedAt(LocalDateTime.now());
        jobSwitchRepository.save(pack);

        return JobSwitchPackDto.builder()
                .id(pack.getId().toString())
                .status(pack.getStatus())
                .downloadUrl(storageService.getPresignedUrl(pack.getBundleKey()))
                .build();
    }

    public List<JobSwitchDownloadDetailsDto> getDownloadDetails(UUID userId) {
        return downloadDetailsRepository.findByUserIdOrderByDownloadedAtDesc(userId).stream()
                .filter(d -> d.getDeletedAt() == null)
                .map(this::toDownloadDto).collect(Collectors.toList());
    }

    public List<JobSwitchDownloadDetailsDto> getAllDownloadDetails() {
        return downloadDetailsRepository.findByDeletedAtIsNullOrderByDownloadedAtDesc().stream()
                .map(this::toDownloadDto).collect(Collectors.toList());
    }

    public void softDeleteDownloadDetail(UUID userId, UUID detailId) {
        var d = downloadDetailsRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Download record not found"));
        if (!d.getUserId().equals(userId)) throw new RuntimeException("Access denied");
        d.setActive(false);
        d.setDeletedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        downloadDetailsRepository.save(d);
    }

    private JobSwitchDownloadDetailsDto toDownloadDto(JobSwitchPackDownloadDetails d) {
        return JobSwitchDownloadDetailsDto.builder()
                .id(d.getId().toString())
                .userId(d.getUserId().toString())
                .packId(d.getPackId().toString())
                .downloadCount(d.getDownloadCount())
                .active(d.getActive())
                .deletedAt(d.getDeletedAt() != null ? d.getDeletedAt().toString() : null)
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt().toString() : null)
                .updatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt().toString() : null)
                .downloadedAt(d.getDownloadedAt() != null ? d.getDownloadedAt().toString() : null)
                .ipAddress(d.getIpAddress())
                .selectedTypesSnapshot(d.getSelectedTypesSnapshot())
                .build();
    }

    private byte[] createZipBundle(
            List<Document> experienceDocs,
            List<Document> payslips,
            List<Document> joiningDocs,
            List<Document> incrementDocs,
            List<Document> offerDocs
    ) throws IOException {
        var baos = new ByteArrayOutputStream();
        List<String> missing = new ArrayList<>();
        try (var zos = new ZipOutputStream(baos)) {
            addDocsToZip(zos, "Experience_Certificates/", experienceDocs, missing);
            addDocsToZip(zos, "Payslips/", payslips, missing);
            addDocsToZip(zos, "Joining_Letters/", joiningDocs, missing);
            addDocsToZip(zos, "Increment_Letters/", incrementDocs, missing);
            addDocsToZip(zos, "Offer_Letters/", offerDocs, missing);
            if (!missing.isEmpty()) {
                var entry = new ZipEntry("missing.txt");
                zos.putNextEntry(entry);
                String content = "Skipped missing files:\n" + String.join("\n", missing);
                zos.write(content.getBytes());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private void addDocsToZip(
            ZipOutputStream zos,
            String folder,
            List<Document> docs,
            List<String> missing
    ) throws IOException {
        for (var doc : docs) {
            try {
                byte[] data = storageService.downloadBytes(doc.getFileKey());
                if (data == null || data.length == 0) {
                    log.warn("Skip empty file for doc {}", doc.getId());
                    missing.add(folder + doc.getFileName() + " (empty)");
                    continue;
                }
                var entry = new ZipEntry(folder + doc.getFileName());
                zos.putNextEntry(entry);
                zos.write(data);
                zos.closeEntry();
            } catch (Exception e) {
                log.warn("Skip missing file for doc {} key {}: {}", doc.getId(), doc.getFileKey(), e.getMessage());
                missing.add(folder + doc.getFileName() + " (missing: " + doc.getId() + ")");
            }
        }
    }
}
