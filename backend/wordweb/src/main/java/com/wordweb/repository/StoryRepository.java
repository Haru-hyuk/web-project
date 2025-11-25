package com.wordweb.repository;

import com.wordweb.entity.Story;
import com.wordweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    List<Story> findAllByUser(User user);
}
