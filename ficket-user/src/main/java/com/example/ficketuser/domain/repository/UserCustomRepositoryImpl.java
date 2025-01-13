package com.example.ficketuser.domain.repository;

import com.example.ficketuser.domain.dto.QUserSimpleDto;
import com.example.ficketuser.domain.dto.UserSimpleDto;
import com.example.ficketuser.domain.dto.resquest.CustomerReq;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.ficketuser.domain.Entity.QUser.user;

@RequiredArgsConstructor
@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserSimpleDto> getUserPage(CustomerReq req, Pageable pageable) {
        return queryFactory.select(new QUserSimpleDto(
                        user.userId,
                        user.birth,
                        user.gender,
                        user.userName,
                        user.socialId
                        ))
                .from(user)
                .where(
                        // userId 조건
                        req.getUserId() != null ? user.userId.eq(req.getUserId()) : null,
                        // userName 조건
                        req.getUserName() != null ? user.userName.contains(req.getUserName()) : null,
                        // createdAt 조건, LocalDate를 LocalDateTime으로 변환
                        req.getStartDate() != null ? user.createdAt.goe(req.getStartDate().atStartOfDay()) : null,
                        req.getEndDate() != null ? user.createdAt.loe(req.getEndDate().atTime(23, 59, 59)) : null
                ).fetch();
    }
}


