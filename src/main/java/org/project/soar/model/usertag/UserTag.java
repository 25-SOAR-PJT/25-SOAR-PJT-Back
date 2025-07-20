package org.project.soar.model.usertag;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.user.User;

@Entity
@Getter
@Setter
@Table(name = "user_tag")
@NoArgsConstructor
public class UserTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public UserTag(User user, Tag tag) {
        this.tag = tag;
        this.user = user;
    }

}
