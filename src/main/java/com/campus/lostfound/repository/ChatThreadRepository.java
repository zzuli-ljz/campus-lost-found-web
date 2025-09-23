package com.campus.lostfound.repository;

import com.campus.lostfound.entity.ChatThread;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {
    List<ChatThread> findByUserAOrUserBOrderByUpdatedAtDesc(User userA, User userB);
    Optional<ChatThread> findByItemAndUserAAndUserB(Item item, User userA, User userB);
    List<ChatThread> findByUserAOrderByUpdatedAtDesc(User userA);
    List<ChatThread> findByUserBOrderByUpdatedAtDesc(User userB);
}






