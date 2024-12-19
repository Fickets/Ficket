package com.example.ficketuser.repository;

import com.example.ficketuser.Entity.User;
import com.example.ficketuser.dto.UserSimpleDto;
import com.example.ficketuser.dto.resquest.CustomerReq;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserCustomRepository {

    List<UserSimpleDto> getUserPage(CustomerReq req, Pageable pageable);
}
