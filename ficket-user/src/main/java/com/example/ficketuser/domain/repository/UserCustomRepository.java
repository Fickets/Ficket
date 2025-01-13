package com.example.ficketuser.domain.repository;

import com.example.ficketuser.domain.dto.UserSimpleDto;
import com.example.ficketuser.domain.dto.resquest.CustomerReq;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserCustomRepository {

    List<UserSimpleDto> getUserPage(CustomerReq req, Pageable pageable);
}
