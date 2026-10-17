package com.generated.app;

public class Note {
    private long id;
    private String title;
    private String content;
    private int colorHex;
    private String imagePath;
    private String audioPath;
    private int audioDuration;
    private boolean isPinned;
    private long updatedAt;

    public Note() {
        this.colorHex = 0xFFFFFFFF; // Default white
        this.updatedAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content == null ? "" : content; }
    public void setContent(String content) { this.content = content; }

    public int getColorHex() { return colorHex; }
    public void setColorHex(int colorHex) { this.colorHex = colorHex; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public int getAudioDuration() { return audioDuration; }
    public void setAudioDuration(int audioDuration) { this.audioDuration = audioDuration; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
