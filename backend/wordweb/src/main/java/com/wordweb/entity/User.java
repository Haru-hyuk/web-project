package com.wordweb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "USERS")   
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "USER_ID")
	private Long userId;


    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "USER_PW", nullable = false)
    private String password;

    @Column(name = "NICKNAME", nullable = false)
    private String nickname;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "USER_BIRTH", nullable = false)
    private String userBirth;

    @Column(name = "PREFERENCE")
    private String preference;

    @Column(name = "GOAL")
    private String goal;

    @Column(name = "DAILY_WORD_GOAL")
    private Integer dailyWordGoal;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;
}
