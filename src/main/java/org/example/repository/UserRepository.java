package org.example.repository;

import org.example.annotation.Cumponent;
import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Cumponent
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
