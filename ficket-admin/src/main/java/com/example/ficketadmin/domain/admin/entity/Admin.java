package com.example.ficketadmin.domain.admin.entity;


import com.example.ficketadmin.global.common.BaseEntity;
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
@SQLDelete(sql = "UPDATE admin SET deleted_at = CURRENT_TIMESTAMP WHERE event_id = ?")
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "PW", nullable = false)
    private String pw;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ROLE", nullable = false)
    private Role role;


}
