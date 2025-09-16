package org.project.soar.model.youthpolicytag.repository;

public interface TagByPolicyProjection {
    String getPolicyId();
    Long getTagId();
    String getTagName();
    Integer getFieldId();
}