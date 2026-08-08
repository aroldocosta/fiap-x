package br.com.fiapx.videoworker.repository;

import br.com.fiapx.videoworker.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
}
