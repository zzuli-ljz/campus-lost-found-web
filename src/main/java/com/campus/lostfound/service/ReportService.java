package com.campus.lostfound.service;

import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.Report;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ItemRepository itemRepository;
    private final NotificationService notificationService;

    @Transactional
    public Report submit(Long itemId, User reporter, String reason) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("物品不存在"));
        Report r = new Report();
        r.setItem(item);
        r.setReporter(reporter);
        r.setReason(reason);
        r.setStatus(Report.Status.SUBMITTED);
        return reportRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<Report> listPending() { return reportRepository.findByStatusOrderByCreatedAtAsc(Report.Status.SUBMITTED); }

    @Transactional
    public void process(Long id, User admin, String note) {
        Report r = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("举报不存在"));
        r.setStatus(Report.Status.PROCESSED);
        r.setProcessedBy(admin);
        r.setProcessNote(note);
        reportRepository.save(r);
    }

    /** 管理员驳回举报，通知举报人 */
    @Transactional
    public void rejectReport(Long id, User admin, String reason) {
        Report r = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("举报不存在"));
        r.setStatus(Report.Status.REJECTED);
        r.setProcessedBy(admin);
        r.setProcessNote(reason);
        reportRepository.save(r);
        notificationService.notifySystemMessage(r.getReporter(), "举报未通过", "您对《" + r.getItem().getTitle() + "》的举报未被采纳。原因：" + (reason==null?"":reason));
    }

    /** 管理员采纳举报并删除物品，通知举报人和发布者 */
    @Transactional
    public void acceptReportAndDeleteItem(Long id, User admin, String reason) {
        Report r = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("举报不存在"));
        Item item = r.getItem();
        r.setStatus(Report.Status.ACCEPTED);
        r.setProcessedBy(admin);
        r.setProcessNote(reason);
        reportRepository.save(r);

        // 通知举报人
        notificationService.notifySystemMessage(r.getReporter(), "举报已受理", "您对《" + item.getTitle() + "》的举报被受理，平台已处理该内容。");
        // 通知发布者
        if (item.getOwner() != null) {
            notificationService.notifySystemMessage(item.getOwner(), "内容被删除", "您的发布《" + item.getTitle() + "》因被举报并经审核确认，已被平台删除。" + (reason==null?"":" 原因："+reason));
        }
        // 删除物品
        // 使用 ItemService 更安全，但此处仅持有仓库，为避免循环依赖，我们可以在上层调用 ItemService
        // 这里简化：直接删除
        itemRepository.delete(item);
    }
}
















