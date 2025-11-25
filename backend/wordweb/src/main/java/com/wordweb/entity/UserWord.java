package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "USER_WORD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "USER_WORD_ID")
	private Long userWordId;

    @ManyToOne
    @JoinColumn(name = "WORD_ID", nullable = false)
    private Word word;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "LAST_RESULT")
    private String lastResult;

    @Column(name = "LAST_STUDY_AT")
    private Timestamp lastStudyAt;

    @Column(name = "TOTAL_CORRECT")
    private Integer totalCorrect;

    @Column(name = "TOTAL_WRONG")
    private Integer totalWrong;
}
