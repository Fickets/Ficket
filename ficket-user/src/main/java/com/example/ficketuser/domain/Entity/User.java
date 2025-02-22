package com.example.ficketuser.domain.Entity;

import com.example.ficketuser.domain.dto.resquest.AdditionalInfoDto;
import com.example.ficketuser.global.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Builder
@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user SET deleted_at = CURRENT_TIMESTAMP, state = 1 WHERE user_id = ?")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "BIRTH", nullable = true)
    private int birth;

    @Column(name = "GENDER", nullable = true)
    private Gender gender;

    @Column(name = "USER_NAME", nullable = true)
    private String userName;

    @Column(name = "SOCIAL_ID", nullable = false)
    private Long socialId;

    @Column(name = "STATE", nullable = false)
    private State state;

    public void addAdditionalInfo(AdditionalInfoDto additionalInfoDto) {
        this.birth = additionalInfoDto.getBirth();
        this.gender = additionalInfoDto.getGender();
    }

    public void updateUserInfo(String updateName, int updateBirth, Gender updateGender) {
        this.userName = updateName;
        this.birth = updateBirth;
        this.gender = updateGender;
    }
}
