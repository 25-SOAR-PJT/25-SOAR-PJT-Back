package org.project.soar.model.banner;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.youthpolicy.YouthPolicy;

@Entity
@Table(name = "banner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Long bannerId;

    @Lob
    @Column(name = "banner_img", columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(name = "content_type")
    private String contentType;

}
