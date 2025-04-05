package com.hbd.coredomain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbd.coredomain.domain.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
