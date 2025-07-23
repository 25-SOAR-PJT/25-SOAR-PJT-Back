package org.project.soar.model.youthpolicytag.repository;

import org.project.soar.model.youthpolicytag.YouthPolicyTag;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface YouthPolicyTagRepository extends JpaRepository<YouthPolicyTag, Long> {

    boolean existsByYouthPolicy(YouthPolicy youthPolicy);
    @Query("SELECT ypt.youthPolicy FROM YouthPolicyTag ypt WHERE ypt.tag.tagId = :tagId")
    List<YouthPolicy> findByTagId(@Param("tagId") Long tagId);

    @Query("SELECT ypt.youthPolicy FROM YouthPolicyTag ypt WHERE ypt.tag.tagId IN :tagIds")
    List<YouthPolicy> findByTagIds(@Param("tagIds") List<Long> tagIds);
}