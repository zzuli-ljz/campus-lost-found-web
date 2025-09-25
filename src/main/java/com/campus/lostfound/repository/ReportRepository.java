package com.campus.lostfound.repository;

import com.campus.lostfound.dto.ReportInfo;
import com.campus.lostfound.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatusOrderByCreatedAtAsc(Report.Status status);

    /**
     * 根据举报ID查询举报信息
     * @param reportId 举报ID
     * @return 一个包含举报信息的DTO
     */
    @Query("SELECT new com.campus.lostfound.dto.ReportInfo(r.item.id, r.reporter.id, r.item.title) FROM Report r WHERE r.id = :reportId")
    ReportInfo findReportInfoAsDTO(@Param("reportId") Long reportId);

    /**
     * 根据ID直接删除举报记录（原生SQL）
     * @param id 举报ID
     */
    @Modifying
    @Query(value = "DELETE FROM reports WHERE id = :id", nativeQuery = true)
    void deleteReportByIdNative(@Param("id") Long id);

    /** 删除与指定物品相关的所有举报 */
    @Modifying
    @Query(value = "DELETE FROM reports WHERE item_id = :itemId", nativeQuery = true)
    void deleteByItemIdNative(@Param("itemId") Long itemId);
}


