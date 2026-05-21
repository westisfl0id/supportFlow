package com.supportflow.ticket.attachment.service;

import com.supportflow.audit.service.AuditLogService;
import com.supportflow.security.CurrentUserService;
import com.supportflow.ticket.attachment.dto.TicketAttachmentFile;
import com.supportflow.ticket.attachment.dto.TicketAttachmentResponse;
import com.supportflow.ticket.attachment.entity.TicketAttachmentEntity;
import com.supportflow.ticket.attachment.exception.AttachmentNotFoundException;
import com.supportflow.ticket.attachment.exception.FileStorageException;
import com.supportflow.ticket.attachment.exception.InvalidAttachmentException;
import com.supportflow.ticket.attachment.repository.TicketAttachmentRepository;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.exception.TicketNotFoundException;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.ticket.service.TicketAccessService;
import com.supportflow.user.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAttachmentService {
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "pdf", "txt", "log"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "application/pdf",
            "text/plain"
    );

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final TicketAccessService ticketAccessService;
    private final AuditLogService auditLogService;

    @Value("${app.upload-dir:uploads/ticket-attachments}")
    private String uploadDir;

    @Transactional
    public TicketAttachmentResponse uploadAttachment(Long ticketId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAttachmentException("Файл не выбран");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidAttachmentException("Размер файла не должен превышать 10 MB");
        }

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        UserEntity currentUser = currentUserService.getCurrentUser();
        ticketAccessService.checkCanViewTicket(currentUser, ticket);

        String originalFilename = cleanFilename(file.getOriginalFilename());
        validateFileType(file, originalFilename);

        String storedFilename = UUID.randomUUID() + "_" + originalFilename;

        try {
            Path uploadPath = getUploadPath();
            Files.createDirectories(uploadPath);

            Path targetPath = uploadPath.resolve(storedFilename).normalize();

            if (!targetPath.startsWith(uploadPath)) {
                throw new InvalidAttachmentException("Некорректное имя файла");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            TicketAttachmentEntity attachment = TicketAttachmentEntity.builder()
                    .ticket(ticket)
                    .uploadedBy(currentUser)
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .storagePath(targetPath.toString())
                    .build();

            attachmentRepository.save(attachment);

            auditLogService.logAttachmentUploaded(ticket, currentUser, originalFilename);

            log.info(
                    "Ticket attachment uploaded: attachmentId={}, ticketId={}, uploadedById={}, filename={}",
                    attachment.getId(),
                    ticket.getId(),
                    currentUser.getId(),
                    originalFilename
            );

            return map(attachment);
        } catch (IOException ex) {
            throw new FileStorageException("Не удалось сохранить файл");
        }
    }

    private void validateFileType(@NotNull MultipartFile file, @NotBlank String filename) {
        String extension = getFileExtension(filename);
        String contentType = file.getContentType();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn(
                    "Attachment rejected: invalid extension={}, filename={}, contentType={}",
                    extension,
                    filename,
                    contentType
            );

            throw new InvalidAttachmentException(
                    "Недопустимый тип файла. Разрешены: png, jpg, jpeg, pdf, txt, log"
            );
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn(
                    "Attachment rejected: invalid contentType={}, filename={}, extension={}",
                    contentType,
                    filename,
                    extension
            );

            throw new InvalidAttachmentException(
                    "Недопустимый MIME-тип файла"
            );
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            throw new InvalidAttachmentException("Файл должен иметь расширение");
        }

        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public List<TicketAttachmentResponse> getTicketAttachments(Long ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        UserEntity currentUser = currentUserService.getCurrentUser();
        ticketAccessService.checkCanViewTicket(currentUser, ticket);

        return attachmentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketAttachmentFile downloadAttachment(Long attachmentId) {
        TicketAttachmentEntity attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));

        UserEntity currentUser = currentUserService.getCurrentUser();
        ticketAccessService.checkCanViewTicket(currentUser, attachment.getTicket());

        try {
            Path filePath = Paths.get(attachment.getStoragePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("Файл недоступен");
            }

            log.info(
                    "Ticket attachment downloaded: attachmentId={}, ticketId={}, downloadedById={}",
                    attachment.getId(),
                    attachment.getTicket().getId(),
                    currentUser.getId()
            );

            return new TicketAttachmentFile(
                    resource,
                    attachment.getContentType(),
                    attachment.getOriginalFilename()
            );
        } catch (MalformedURLException ex) {
            log.error("Failed to download ticket attachment: attachmentId={}, storagePath={}", attachmentId, attachment.getStoragePath(), ex);
            throw new FileStorageException("Не удалось загрузить файл");
        }
    }

    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String cleanFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }

        return Paths.get(filename).getFileName().toString();
    }

    private TicketAttachmentResponse map(TicketAttachmentEntity attachment) {
        return new TicketAttachmentResponse(
                attachment.getId(),
                attachment.getTicket().getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getUploadedBy().getId(),
                attachment.getUploadedBy().getName(),
                attachment.getCreatedAt()
        );
    }
}
