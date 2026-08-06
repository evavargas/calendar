package app.kairo.google;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleConnectionRepository extends JpaRepository<GoogleConnectionEntity, UUID> {}
