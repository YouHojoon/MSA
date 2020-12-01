package com.thoughtmechanix.authentication.repository;

import com.thoughtmechanix.authentication.domain.OrgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface OrgUserRepository extends JpaRepository<OrgUser, Object> {
    Optional<OrgUser> findByUserName(String userName);
}
