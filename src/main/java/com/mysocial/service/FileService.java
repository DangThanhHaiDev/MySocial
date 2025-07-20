package com.mysocial.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {
    private static final String UPLOAD_DIR = "uploads/";


    private static final String UPLOAD_DIR_ICON = "uploads/icon/";
    private static final String UPLOAD_DIR_MESSAGE = "uploads/message";
    private final Path uploadDir = Paths.get(UPLOAD_DIR_MESSAGE);


    // Loại file được cho phép
    private static final List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/jpg", "image/webp");
    private static final List<String> allowedTypesIcon = List.of("image/jpeg", "image/png", "image/jpg", "image/webp", "image/gif");
    private static final List<String> allowedTypesMessage = List.of("image/jpeg", "image/png", "image/jpg", "image/webp", "image/gif");


    public String saveImage(MultipartFile file) throws IOException {
        // Kiểm tra định dạng file
        if (!allowedTypes.contains(file.getContentType())) {
            throw new IOException("File không đúng định dạng hình ảnh");
        }

        // Giới hạn dung lượng (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("File quá lớn (tối đa 5MB)");
        }

        // Tạo thư mục nếu chưa có
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // Tạo tên file duy nhất
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(file.getOriginalFilename().lastIndexOf(".")))
                .orElse(".jpg");

        String filename = UUID.randomUUID().toString() + ext;

        Path filepath = Paths.get(UPLOAD_DIR, filename);
        Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn để frontend dùng
        return "/uploads/" + filename;
    }
    //Lưu icon
    public String saveIcon(MultipartFile file) throws IOException {
        // Kiểm tra định dạng file
        if (!allowedTypesIcon.contains(file.getContentType())) {
            throw new IOException("File không đúng định dạng hình ảnh");
        }

        // Giới hạn dung lượng (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("File quá lớn (tối đa 5MB)");
        }

        // Tạo thư mục nếu chưa có
        File uploadDir = new File(UPLOAD_DIR_ICON);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // Tạo tên file duy nhất
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(file.getOriginalFilename().lastIndexOf(".")))
                .orElse(".jpg");

        String filename = UUID.randomUUID().toString() + ext;

        Path filepath = Paths.get(UPLOAD_DIR_ICON, filename);
        Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn để frontend dùng
        return "/uploads/icon/" + filename;
    }

    public String saveMessage(MultipartFile file) throws IOException {
        // Kiểm tra định dạng file
        if (!allowedTypesMessage.contains(file.getContentType())) {
            throw new IOException("File không đúng định dạng hình ảnh");
        }

        // Giới hạn dung lượng (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("File quá lớn (tối đa 5MB)");
        }

        // Tạo thư mục nếu chưa có
        File uploadDir = new File(UPLOAD_DIR_MESSAGE);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // Tạo tên file duy nhất
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(file.getOriginalFilename().lastIndexOf(".")))
                .orElse(".jpg");

        String filename = UUID.randomUUID().toString() + ext;

        Path filepath = Paths.get(UPLOAD_DIR_MESSAGE, filename);
        Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/message/" + filename;
    }

    public List<String> saveImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(saveImage(file));
        }
        return urls;
    }

    public String saveBase64File(String base64, String fileName) {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            Path path = uploadDir.resolve(UUID.randomUUID() + "_" + fileName);
            Files.write(path, decodedBytes);
            System.out.println("Saving file to: " + path.toAbsolutePath());
            return "/uploads/message/" + path.getFileName().toString();
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file", e);
        }
    }
}
