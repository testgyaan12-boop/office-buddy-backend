package com.officebuddy.jobswitch;

import com.officebuddy.document.Document;
import com.officebuddy.document.DocumentRepository;
import com.officebuddy.document.DocumentType;
import com.officebuddy.jobswitch.dto.JobSwitchPackDto;
import com.officebuddy.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class JobSwitchService {

    private final JobSwitchRepository jobSwitchRepository;
    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    public JobSwitchPackDto generatePack(UUID userId) {
        var documents = documentRepository.findByUserIdOrderByUploadedAtDesc(userId);

        var experienceDocs = documents.stream()
                .filter(d -> d.getType() == DocumentType.CERTIFICATE ||
                             d.getType() == DocumentType.RELIEVING_LETTER)
                .toList();

        var payslips = documents.stream()
                .filter(d -> d.getType() == DocumentType.PAYSLIP)
                .limit(3)
                .toList();

        var joiningDocs = documents.stream()
                .filter(d -> d.getType() == DocumentType.JOINING_LETTER)
                .toList();

        var incrementDocs = documents.stream()
                .filter(d -> d.getType() == DocumentType.INCREMENT_LETTER)
                .toList();

        var offerDocs = documents.stream()
                .filter(d -> d.getType() == DocumentType.OFFER_LETTER)
                .toList();

        try {
            var zipBytes = createZipBundle(experienceDocs, payslips, joiningDocs, incrementDocs, offerDocs);

            var fileName = "job-switch-pack-" + UUID.randomUUID() + ".zip";
            var result = storageService.uploadBytes(fileName, zipBytes, "application/zip");

            var pack = JobSwitchPack.builder()
                    .userId(userId)
                    .status("READY")
                    .bundleKey(result.getKey())
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

    public JobSwitchPackDto getPackStatus(UUID userId) {
        var pack = jobSwitchRepository.findTopByUserIdOrderByGeneratedAtDesc(userId);
        if (pack.isEmpty()) {
            return JobSwitchPackDto.builder().status("NOT_FOUND").build();
        }

        var p = pack.get();
        return JobSwitchPackDto.builder()
                .id(p.getId().toString())
                .status(p.getStatus())
                .downloadUrl(storageService.getPresignedUrl(p.getBundleKey()))
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
        try (var zos = new ZipOutputStream(baos)) {
            addDocsToZip(zos, "Experience_Certificates/", experienceDocs);
            addDocsToZip(zos, "Payslips/", payslips);
            addDocsToZip(zos, "Joining_Letters/", joiningDocs);
            addDocsToZip(zos, "Increment_Letters/", incrementDocs);
            addDocsToZip(zos, "Offer_Letters/", offerDocs);
        }
        return baos.toByteArray();
    }

    private void addDocsToZip(
            ZipOutputStream zos,
            String folder,
            List<Document> docs
    ) throws IOException {
        for (var doc : docs) {
            var entry = new ZipEntry(folder + doc.getFileName());
            zos.putNextEntry(entry);
            zos.write(("Placeholder for: " + doc.getTitle()).getBytes());
            zos.closeEntry();
        }
    }
}
