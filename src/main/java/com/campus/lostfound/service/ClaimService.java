package com.campus.lostfound.service;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.ClaimRepository;
import com.campus.lostfound.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final NotificationService notificationService;
    private final com.campus.lostfound.repository.UserRepository userRepository;

    @Transactional
    public Claim submitClaim(Long itemId, User claimant, String verificationDetail) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("物品不存在"));
        if (!item.canBeClaimed()) {
            throw new RuntimeException("该物品当前不可认领");
        }
        if (item.getOwner().getId().equals(claimant.getId())) {
            throw new RuntimeException("不能认领自己发布的物品");
        }
        boolean exists = claimRepository.existsByItemAndClaimant(item, claimant);
        if (exists) {
            throw new RuntimeException("你已提交过该物品的认领申请");
        }
        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimant(claimant);
        claim.setVerificationDetail(verificationDetail);
        claim.setStatus(Claim.ClaimStatus.SUBMITTED);
        Claim saved = claimRepository.save(claim);
        log.info("认领申请已提交: item={}, user={}", item.getId(), claimant.getUsername());
        // 通知发布者有新的认领申请
        notificationService.notifyClaimSubmitted(item, claimant);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Claim> listPending() {
        // 仅展示已被发布者同意的申请，等待管理员终审
        return claimRepository.findByStatusOrderByCreatedAtAsc(Claim.ClaimStatus.SUBMITTED)
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getOwnerConfirmed()))
                .toList();
    }

    @Transactional
    public void reviewClaim(Long claimId, User admin, boolean pass, String note) {
        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new RuntimeException("认领不存在"));
        if (claim.getStatus() != Claim.ClaimStatus.SUBMITTED) {
            log.warn("认领状态非 SUBMITTED，忽略。claimId={}, status={}", claimId, claim.getStatus());
            return;
        }
        // 使用持久化的管理员实体，避免瞬态/游离实体引起的持久化错误
        try {
            if (admin != null && admin.getId() != null) {
                com.campus.lostfound.entity.User managed = userRepository.findById(admin.getId()).orElse(null);
                if (managed != null) claim.setReviewedBy(managed);
            }
        } catch (Exception ignored) {}
        claim.setReviewedAt(LocalDateTime.now());
        claim.setReviewNote(note);
        if (pass) {
            // 最终审核通过即流程结束：认领完成 + 物品标记已完成
            claim.setStatus(Claim.ClaimStatus.COMPLETED);
            claim.setCompletedAt(LocalDateTime.now());
            Item item = claim.getItem();
            item.setStatus(Item.ItemStatus.COMPLETED);
            try { itemRepository.save(item); } catch (Exception e) { log.error("保存物品失败: {}", e.getMessage(), e); }
            try { claimRepository.save(claim); } catch (Exception e) { log.error("保存认领失败: {}", e.getMessage(), e); }
            // 通知双方（失败不影响主流程）
            try { notificationService.notifyClaimApproved(claim.getClaimant(), item); } catch (Exception e) { log.error("通知申请者失败: {}", e.getMessage()); }
            try { notificationService.notifySystemMessage(item.getOwner(), "认领已完成", "您的发布《" + item.getTitle() + "》已通过管理员审核并完成。"); } catch (Exception e) { log.error("通知发布者失败: {}", e.getMessage()); }
        } else {
            // 管理员拒绝：标记后删除记录，并通知双方
            claim.setStatus(Claim.ClaimStatus.REJECTED);
            try { claimRepository.save(claim); } catch (Exception e) { log.error("保存认领失败: {}", e.getMessage(), e); }
            try { claimRepository.delete(claim); } catch (Exception e) { log.error("删除认领失败: {}", e.getMessage(), e); }
            try { notificationService.notifyClaimRejected(claim.getClaimant(), claim.getItem(), note == null?"":note); } catch (Exception e) { log.error("通知申请者失败: {}", e.getMessage()); }
            try { notificationService.notifySystemMessage(claim.getItem().getOwner(), "管理员驳回认领", "管理员驳回了该条认领申请：" + (note==null?"":note)); } catch (Exception e) { log.error("通知发布者失败: {}", e.getMessage()); }
        }
    }

    /** 发布者确认认领（同意/驳回），产生通知并供管理员查看 */
    @Transactional
    public void ownerConfirm(Long claimId, User owner, boolean agree, String note) {
        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new RuntimeException("认领不存在"));
        if (!claim.getItem().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("无权确认该认领");
        }
        claim.setOwnerConfirmed(agree);
        claim.setOwnerConfirmNote(note);
        claim.setOwnerConfirmedAt(LocalDateTime.now());
        claimRepository.save(claim);

        // 通知与后续动作
        if (agree) {
            notificationService.notifySystemMessage(claim.getClaimant(), "发布者已确认", "您对《" + claim.getItem().getTitle() + "》的认领已被发布者确认，等待管理员审核。");
        } else {
            notificationService.notifySystemMessage(claim.getClaimant(), "发布者已驳回", "发布者对您对《" + claim.getItem().getTitle() + "》的认领做出了驳回：" + (note==null?"":note));
            // 发布者驳回：删除该申请，并通知发布者
            claimRepository.delete(claim);
            notificationService.notifySystemMessage(owner, "已驳回认领", "您已驳回来自 " + claim.getClaimant().getDisplayName() + " 的认领申请。");
        }
    }

    @Transactional
    public void completeClaim(Long claimId, User admin) {
        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new RuntimeException("认领不存在"));
        if (claim.getStatus() != Claim.ClaimStatus.APPROVED) return;
        claim.setCompletedAt(LocalDateTime.now());
        claim.setStatus(Claim.ClaimStatus.COMPLETED);
        claimRepository.save(claim);
        // 同时更新物品状态为已认领
        Item item = claim.getItem();
        item.setStatus(Item.ItemStatus.CLAIMED);
        itemRepository.save(item);
        notificationService.notifySystemMessage(claim.getItem().getOwner(), "物品已被认领", "您的物品《" + item.getTitle() + "》已完成认领");
    }
}


