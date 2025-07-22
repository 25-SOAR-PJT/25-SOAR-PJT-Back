package org.project.soar.model.usertag.repository;

import org.project.soar.model.tag.Tag;
import org.project.soar.model.usertag.UserTag;
import org.project.soar.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findByUser(User user);

    @Query("SELECT ut.tag FROM UserTag ut WHERE ut.user = :user")
    List<Tag> findAllTagByUser(@Param("user") User user);

    @Query("SELECT ut.tag FROM UserTag ut WHERE ut.user.userId = :userId")
    List<Tag> findAllTagByUserId(@Param("userId") Long userId);

    void deleteAllByUser(User user);
}
