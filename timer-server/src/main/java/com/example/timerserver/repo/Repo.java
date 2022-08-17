package com.example.timerserver.repo;

import com.example.timerserver.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Repo extends JpaRepository<Event, String> {
}
