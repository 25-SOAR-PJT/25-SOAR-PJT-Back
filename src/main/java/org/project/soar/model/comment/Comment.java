package org.project.soar.model.comment;

import lombok.Data;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Comment")
@Data
public class Comment {
    @Id
    @Column(name = "comment_id")
    private Integer commentId;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "comment")
    private String comment;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private YouthPolicy youthPolicy;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
