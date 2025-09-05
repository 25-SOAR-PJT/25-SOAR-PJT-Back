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

    @Query("""
           select ypt.youthPolicy as youthPolicy,
                  count(distinct ypt.tag.tagId) as matchCount
           from YouthPolicyTag ypt
           where ypt.tag.tagId in :tagIds
           group by ypt.youthPolicy
           """)
    List<PolicyTagMatchProjection> findPolicyMatchCounts(@Param("tagIds") List<Long> tagIds);


    // (기존) matches 등 다른 메서드가 이미 있다면 그대로 두고 아래만 추가
    @Query("""
        SELECT ypt.youthPolicy.policyId AS policyId,
               t.tagId                  AS tagId,
               t.tagName                AS tagName
        FROM YouthPolicyTag ypt
        JOIN ypt.tag t
        WHERE ypt.youthPolicy.policyId IN :policyIds
    """)
    List<TagByPolicyProjection> findTagsByPolicyIds(@Param("policyIds") List<String> policyIds);
}