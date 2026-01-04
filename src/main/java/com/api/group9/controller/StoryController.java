package com.api.group9.controller;

import com.api.group9.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createStory(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "musicUrl", required = false) String musicUrl,
            @RequestParam(value = "musicTitle", required = false) String musicTitle,
            @RequestParam(value = "artistName", required = false) String artistName,
            Principal principal) {
            
        try {
            storyService.createStory(principal.getName(), file, caption, musicUrl, musicTitle, artistName);
            return ResponseEntity.ok("Đăng story thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi upload: " + e.getMessage());
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getActiveStories() {
        try {
            return ResponseEntity.ok(storyService.getActiveStories());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi tải story: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStory(@PathVariable Long id, Principal principal) {
        try {
            storyService.deleteStory(id, principal.getName());
            return ResponseEntity.ok("Đã xóa Story thành công!");
        } catch (RuntimeException e) {
            // Xử lý thông báo lỗi tùy theo Service ném ra
            if (e.getMessage().startsWith("FORBIDDEN")) {
                return ResponseEntity.status(403).body(e.getMessage());
            } else if (e.getMessage().startsWith("NOT_FOUND")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(500).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}