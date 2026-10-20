package de.uxd.vibetracker.anchor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnchorRepository extends JpaRepository<AnchorSlot, AnchorKey> {
}
