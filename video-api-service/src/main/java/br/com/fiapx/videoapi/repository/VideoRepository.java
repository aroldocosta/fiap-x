package br.com.fiapx.videoapi.repository;

import br.com.fiapx.videoapi.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    List<Video> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Video> findByIdAndUserId(UUID id, UUID userId);
}
