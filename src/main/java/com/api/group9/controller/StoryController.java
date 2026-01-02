package com.api.group9.controller;

import com.api.group9.model.Story;
import com.api.group9.model.User;
import com.api.group9.repository.StoryRepository;
import com.api.group9.repository.UserRepository;
import com.api.group9.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Collectors;
import java.security.Principal;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryRepository storyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createStory(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "musicUrl", required = false) String musicUrl,
            @RequestParam(value = "musicTitle", required = false) String musicTitle,
            @RequestParam(value = "artistName", required = false) String artistName,
            Principal principal) {
            
            try {
                User user = userRepository.findByEmail(principal.getName()).orElseThrow();
                Story story = new Story();
                story.setUser(user);
                story.setCaption(caption);
                story.setMusicUrl(musicUrl);
                story.setMusicTitle(musicTitle);
                story.setArtistName(artistName);

                String contentType = file.getContentType();
                String uploadedUrl = "";

                if (contentType != null && contentType.startsWith("video")) {
                    story.setMediaType(Story.MediaType.VIDEO);
                    uploadedUrl = cloudinaryService.uploadVideo(file);
                } else {
                    story.setMediaType(Story.MediaType.IMAGE);
                    uploadedUrl = cloudinaryService.uploadImage(file);
                }
                
                story.setMediaUrl(uploadedUrl);
                storyRepository.save(story);
                return ResponseEntity.ok("Đăng story thành công!");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Lỗi upload: " + e.getMessage());
            }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getActiveStories() {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            java.util.List<Story> stories = storyRepository.findAll().stream()
                    .filter(s -> s.getExpiredAt() != null && s.getExpiredAt().isAfter(now))
                    .collect(Collectors.toList());;

            java.util.List<com.api.group9.dto.Response.StoryResponse> response = stories.stream()
                    .map(com.api.group9.dto.Response.StoryResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi tải story: " + e.getMessage());
        }
    }

    // --- THÊM HÀM XÓA Ở ĐÂY ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStory(@PathVariable Long id, Principal principal) {
        try {
            // 1. Tìm Story trong DB
            Story story = storyRepository.findById(id).orElse(null);
            if (story == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. CHECK QUYỀN CHÍNH CHỦ (Quan trọng)
            // Lấy email người đang đăng nhập so sánh với email người tạo story
            String currentUserEmail = principal.getName();
            String ownerEmail = story.getUser().getEmail();

            if (!currentUserEmail.equals(ownerEmail)) {
                return ResponseEntity.status(403).body("Bạn không có quyền xóa Story này!");
            }

            // 3. Xóa file trên Cloudinary (Dọn rác)
            try {
                // Phải check loại media để gọi hàm xóa đúng (vì video và ảnh Cloudinary xử lý khác nhau)
                if (story.getMediaType() == Story.MediaType.VIDEO) {
                    cloudinaryService.deleteVideoByUrl(story.getMediaUrl());
                } else {
                    cloudinaryService.deleteImageByUrl(story.getMediaUrl());
                }
            } catch (Exception e) {
                // Nếu xóa trên cloud lỗi thì log ra thôi, vẫn tiếp tục xóa trong DB
                System.err.println("Lỗi xóa file trên Cloudinary: " + e.getMessage());
            }

            // 4. Xóa trong Database
            storyRepository.delete(story);

            return ResponseEntity.ok("Đã xóa Story thành công!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}