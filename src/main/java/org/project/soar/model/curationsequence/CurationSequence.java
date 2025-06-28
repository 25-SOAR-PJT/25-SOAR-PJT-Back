package org.project.soar.model.curationsequence;

import jakarta.persistence.*;
import lombok.*;
import org.project.soar.model.user.User;
import org.project.soar.global.abstracts.BaseTimeEntity;

@Entity
@Table(name = "curation_sequence")
@Getter
@Setter

@Builder
public class CurationSequence extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_id")
    private Long seqId;

    @Column(name = "seq_residence", length = 100)
    private String seqResidence;

    @Column(name = "seq_occupation", length = 100)
    private String seqOccupation;

    @Column(name = "seq_education_level", length = 50)
    private String seqEducationLevel;

    @Column(name = "seq_additional_criteria", length = 200)
    private String seqAdditionalCriteria;

    @Column(name = "seq_income_bracket", length = 50)
    private String seqIncomeBracket;

    @Column(name = "seq_keywords", length = 500)
    private String seqKeywords;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}