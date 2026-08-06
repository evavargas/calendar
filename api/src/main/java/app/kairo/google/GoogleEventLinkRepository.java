package app.kairo.google;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleEventLinkRepository extends JpaRepository<GoogleEventLinkEntity, UUID> {}
