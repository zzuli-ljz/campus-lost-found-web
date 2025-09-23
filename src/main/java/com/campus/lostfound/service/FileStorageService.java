package com.campus.lostfound.service;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.ItemImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务类
 */
@Service
@Slf4j
public class FileStorageService {
    
    @Value("${app.upload.path:uploads/}")
    private String uploadPath;
    
    @Value("${app.upload.max-size:10485760}")
    private long maxFileSize;
    
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    /**
     * 保存物品图片
     */
    public List<ItemImage> saveItemImages(Item item, List<MultipartFile> images) throws IOException {
        List<ItemImage> savedImages = new ArrayList<>();
        
        if (images == null || images.isEmpty()) {
            return savedImages;
        }
        
        // 创建物品专用目录
        String itemDir = uploadPath + "items/" + item.getId() + "/";
        Path itemPath = Paths.get(itemDir);
        Files.createDirectories(itemPath);
        
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            
            if (image.isEmpty()) {
                continue;
            }
            
            // 验证文件类型
            if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
                throw new IllegalArgumentException("不支持的文件类型: " + image.getContentType());
            }
            
            // 验证文件大小
            if (image.getSize() > maxFileSize) {
                throw new IllegalArgumentException("文件大小超过限制: " + image.getSize());
            }
            
            // 生成唯一文件名
            String originalFilename = image.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path targetPath = itemPath.resolve(filename);
            Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 创建图片记录
            ItemImage itemImage = new ItemImage();
            itemImage.setFileName(filename);
            itemImage.setOriginalFileName(originalFilename);
            itemImage.setFilePath(targetPath.toString());
            itemImage.setFileSize(image.getSize());
            itemImage.setMimeType(image.getContentType());
            itemImage.setItem(item);
            itemImage.setSortOrder(i);
            itemImage.setIsMain(i == 0); // 第一张图片设为主图
            
            savedImages.add(itemImage);
            
            log.info("保存物品图片: {} -> {}", originalFilename, filename);
        }
        
        return savedImages;
    }
    
    /**
     * 删除物品图片
     */
    public void deleteItemImages(Item item) throws IOException {
        String itemDir = uploadPath + "items/" + item.getId() + "/";
        Path itemPath = Paths.get(itemDir);
        
        if (Files.exists(itemPath)) {
            // 删除目录下的所有文件
            Files.walk(itemPath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.info("删除文件: {}", path);
                    } catch (IOException e) {
                        log.error("删除文件失败: {}", path, e);
                    }
                });
            
            // 删除目录
            Files.deleteIfExists(itemPath);
            log.info("删除物品图片目录: {}", itemDir);
        }
    }
    
    /**
     * 删除单个图片
     */
    public void deleteImage(ItemImage itemImage) throws IOException {
        Path imagePath = Paths.get(itemImage.getFilePath());
        
        if (Files.exists(imagePath)) {
            Files.delete(imagePath);
            log.info("删除图片: {}", itemImage.getFileName());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex);
    }
    
    /**
     * 验证图片文件
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // 检查文件类型
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            return false;
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取文件大小描述
     */
    public String getFileSizeDescription(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 创建缩略图
     */
    public void createThumbnail(ItemImage itemImage, int width, int height) {
        // 这里可以集成图片处理库来创建缩略图
        // 例如使用 Thumbnailator 或 ImageIO
        log.info("创建缩略图: {} -> {}x{}", itemImage.getFileName(), width, height);
    }
}
