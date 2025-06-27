package org.project.soar.model.curationsequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.project.soar.model.user.User;


@Entity
@Table(name = "CurationSequence")
@Data
public class CurationSequence {
    @Id
    @Column(name = "seq_id")
    private String seqId;

    @Column(name = "seq_residence")
    private String seqResidence;

    @Column(name = "seq_occupation")
    private String seqOccupation;

    @Column(name = "seq_education_level")
    private String seqEducationLevel;

    @Column(name = "seq_additional_criteria")
    private String seqAdditionalCriteria;

    @Column(name = "seq_income_bracket")
    private String seqIncomeBracket;

    @Column(name = "seq_keywords")
    private String seqKeywords;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
