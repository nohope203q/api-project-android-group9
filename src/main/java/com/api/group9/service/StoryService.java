package com.api.group9.service;

import com.api.group9.dto.Response.StoryResponse;
import com.api.group9.model.Story;
import com.api.group9.model.User;
import com.api.group9.repository.StoryRepository;
import com.api.group9.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Logic tạo Story
    public void createStory(String email, MultipartFile file
                           ) throws Exception {
        
        User user = userRepository.findByEmail(email).orElseThrow();
        Story story = new Story();
        story.setUser(user);

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
    }

    public List<StoryResponse> getActiveStories() {
        LocalDateTime now = LocalDateTime.now();

        List<Story> stories = storyRepository.findAll().stream()
                .filter(s -> s.getExpiredAt() != null && s.getExpiredAt().isAfter(now))
                .collect(Collectors.toList());

        return stories.stream()
                .map(StoryResponse::new)
                .collect(Collectors.toList());
    }

    // Logic xóa Story
    public void deleteStory(Long id, String currentUserEmail) throws Exception {
        // 1. Tìm Story
        Story story = storyRepository.findById(id).orElse(null);
        if (story == null) {
            throw new RuntimeException("NOT_FOUND: Story không tồn tại");
        }

        // 2. Check quyền chính chủ
        String ownerEmail = story.getUser().getEmail();
        if (!currentUserEmail.equals(ownerEmail)) {
            throw new RuntimeException("FORBIDDEN: Bạn không có quyền xóa Story này!");
        }

        // 3. Xóa file trên Cloudinary
        try {
            if (story.getMediaType() == Story.MediaType.VIDEO) {
                cloudinaryService.deleteVideoByUrl(story.getMediaUrl());
            } else {
                cloudinaryService.deleteImageByUrl(story.getMediaUrl());
            }
        } catch (Exception e) {
            System.err.println("Lỗi xóa file trên Cloudinary: " + e.getMessage());
        }

        // 4. Xóa trong Database
        storyRepository.delete(story);
    }
}