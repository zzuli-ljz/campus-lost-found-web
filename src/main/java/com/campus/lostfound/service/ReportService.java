package com.campus.lostfound.service;

import com.campus.lostfound.dto.ReportInfo;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.Report;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.ReportRepository;
import com.campus.lostfound.repository.UserRepository;
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
    private final ItemService itemService;
    private final UserRepository userRepository;

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
    public void rejectReport(Long reportId, User admin, String reason) {
        ReportInfo reportInfo = reportRepository.findReportInfoAsDTO(reportId);
        if (reportInfo == null) {
            throw new RuntimeException("举报不存在");
        }
        User reporter = userRepository.findById(reportInfo.getReporterId()).orElse(null);
        String itemTitle = reportInfo.getItemTitle();

        reportRepository.deleteReportByIdNative(reportId);

        if (reporter != null) {
            notificationService.notifySystemMessage(reporter, "举报未通过", "您对《" + itemTitle + "》的举报未被采纳。原因：" + (reason == null ? "" : reason));
        }
    }

    /** 管理员采纳举报并删除物品，通知举报人和发布者 */
    @Transactional
    public void acceptReportAndDeleteItem(Long reportId, User admin, String reason) {
        ReportInfo reportInfo = reportRepository.findReportInfoAsDTO(reportId);
        if (reportInfo == null) {
            throw new RuntimeException("举报不存在");
        }
        Long itemId = reportInfo.getItemId();
        User reporter = userRepository.findById(reportInfo.getReporterId()).orElse(null);
        String itemTitle = reportInfo.getItemTitle();
        User itemOwner = itemRepository.findById(itemId).map(Item::getOwner).orElse(null);

        // 删除物品
        itemService.deleteItem(itemId);

        // 删除举报
        reportRepository.deleteReportByIdNative(reportId);

        // 通知举报人
        if (reporter != null) {
            notificationService.notifySystemMessage(reporter, "举报已受理", "您对《" + itemTitle + "》的举报被受理，平台已处理该内容。");
        }
        // 通知发布者
        if (itemOwner != null) {
            notificationService.notifySystemMessage(itemOwner, "内容被删除", "您的发布《" + itemTitle + "》因被举报并经审核确认，已被平台删除。" + (reason == null ? "" : " 原因：" + reason));
        }
    }
}
















