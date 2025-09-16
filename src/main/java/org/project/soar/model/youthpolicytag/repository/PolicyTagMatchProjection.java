package org.project.soar.model.youthpolicytag.repository;

import org.project.soar.model.youthpolicy.YouthPolicy;

public interface PolicyTagMatchProjection {
    YouthPolicy getYouthPolicy(); // alias: youthPolicy
    Long getMatchCount();         // alias: matchCount
}