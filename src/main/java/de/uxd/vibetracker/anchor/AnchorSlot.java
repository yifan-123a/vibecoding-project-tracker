package de.uxd.vibetracker.anchor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * One pinned deliverable link. The key is the primary key, so there are exactly
 * four rows and no way to create a fifth.
 */
@Entity
@Table(name = "anchor_slots")
public class AnchorSlot {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_key", length = 24)
    private AnchorKey key;

    @Column(length = 500)
    private String url;

    private LocalDateTime updatedAt;

    protected AnchorSlot() {
        // for JPA
    }

    public AnchorSlot(AnchorKey key) {
        this.key = key;
    }

    public AnchorKey getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
