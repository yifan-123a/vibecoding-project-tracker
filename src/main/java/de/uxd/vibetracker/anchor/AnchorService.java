package de.uxd.vibetracker.anchor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Owns the four pinned deliverable links (M11).
 *
 * Rule: a link is either a real http(s) URL or it does not exist. Blank clears
 * the slot — that is different from a malformed value, which is rejected and the
 * previous link stays.
 */
@Service
public class AnchorService {

    private static final Pattern HTTP_URL = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    private final AnchorRepository repository;

    public AnchorService(AnchorRepository repository) {
        this.repository = repository;
    }

    /** Always four, in declaration order, created on first read. */
    @Transactional
    public List<AnchorSlot> all() {
        List<AnchorSlot> slots = new ArrayList<>(AnchorKey.values().length);
        for (AnchorKey key : AnchorKey.values()) {
            slots.add(repository.findById(key).orElseGet(() -> repository.save(new AnchorSlot(key))));
        }
        return slots;
    }

    public List<AnchorView> views() {
        return all().stream()
                .map(slot -> new AnchorView(
                        slot.getKey().name(),
                        slot.getKey().getLabel(),
                        slot.getKey().getHint(),
                        slot.getUrl(),
                        isFilled(slot.getUrl()),
                        slot.getUpdatedAt() == null ? null : slot.getUpdatedAt().toString()))
                .toList();
    }

    /**
     * @return false when the value is neither blank nor a valid http(s) URL —
     *         the caller turns that into a message instead of a silent no-op.
     */
    @Transactional
    public boolean save(AnchorKey key, String rawUrl) {
        String value = rawUrl == null ? "" : rawUrl.trim();
        if (!value.isEmpty() && !HTTP_URL.matcher(value).matches()) {
            return false;
        }

        AnchorSlot slot = repository.findById(key).orElseGet(() -> repository.save(new AnchorSlot(key)));
        slot.setUrl(value.isEmpty() ? null : value);
        slot.setUpdatedAt(value.isEmpty() ? null : LocalDateTime.now());
        repository.save(slot);
        return true;
    }

    public Optional<AnchorSlot> find(AnchorKey key) {
        return repository.findById(key);
    }

    static boolean isFilled(String url) {
        return url != null && !url.isBlank();
    }

    public static String labelOf(AnchorKey key) {
        return key.getLabel().toLowerCase(Locale.ROOT);
    }
}
