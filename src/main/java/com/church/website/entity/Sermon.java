package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "sermon")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sermon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String preacher;

    @Column(nullable = false)
    private LocalDate sermonDate;

    @Column(nullable = false, length = 200)
    private String biblePassage;

    @Column
    private String videoUrl;

    @Column
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getYoutubeVideoId() {
        if (videoUrl == null) return null;
        Pattern pattern = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
        );
        Matcher matcher = pattern.matcher(videoUrl);
        return matcher.find() ? matcher.group(1) : null;
    }
}
