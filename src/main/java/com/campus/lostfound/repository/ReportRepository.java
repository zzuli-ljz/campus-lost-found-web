package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Report;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatusOrderByCreatedAtAsc(Report.Status status);

    /** 删除与指定物品相关的所有举报 */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Report r WHERE r.item = :item")
    int deleteByItem(@org.springframework.data.repository.query.Param("item") Item item);
}


