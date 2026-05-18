package com.hostelfinder.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
  private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
  private final Path baseDir;

  public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
    this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  public StoredFile save(MultipartFile file, String subdir) throws IOException {
    if (file == null || file.isEmpty())
      throw new IOException("Empty file");
    String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
    String ext = "";
    int dot = original.lastIndexOf('.');
    if (dot >= 0 && dot < original.length() - 1)
      ext = original.substring(dot);

    String name = UUID.randomUUID() + ext.toLowerCase(Locale.ROOT);
    Path dir = baseDir.resolve(subdir).normalize();
    Files.createDirectories(dir);
    Path target = dir.resolve(name).normalize();
    Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

    String url = "/uploads/" + subdir + "/" + name;
    return new StoredFile(url, file.getContentType(), file.getSize());
  }

  public boolean isSupportedImageFilename(String filename) {
    if (!StringUtils.hasText(filename)) return false;
    String clean = StringUtils.cleanPath(filename);
    int dot = clean.lastIndexOf('.');
    if (dot < 0 || dot == clean.length() - 1) return false;
    String ext = clean.substring(dot).toLowerCase(Locale.ROOT);
    return SUPPORTED_IMAGE_EXTENSIONS.contains(ext);
  }

  public void deleteByUrl(String url) throws IOException {
    if (!StringUtils.hasText(url) || !url.startsWith("/uploads/")) {
      return;
    }
    String relative = url.substring("/uploads/".length());
    Path target = baseDir.resolve(relative).normalize();
    if (target.startsWith(baseDir)) {
      Files.deleteIfExists(target);
    }
  }

  public record StoredFile(String url, String mime, long sizeBytes) {
  }
}
