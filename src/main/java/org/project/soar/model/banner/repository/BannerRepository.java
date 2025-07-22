package org.project.soar.model.banner.repository;

import org.project.soar.model.banner.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {
}
