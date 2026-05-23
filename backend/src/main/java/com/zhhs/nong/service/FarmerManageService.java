package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.admin.BatchReviewRequest;
import com.zhhs.nong.mapper.FarmerVerificationMapper;
import com.zhhs.nong.mapper.UserMapper;
import com.zhhs.nong.model.FarmerVerification;
import com.zhhs.nong.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FarmerManageService {

    private final FarmerVerificationMapper farmerVerificationMapper;
    private final UserMapper userMapper;
    private final OperationLogService logService;

    public FarmerManageService(FarmerVerificationMapper farmerVerificationMapper,
                               UserMapper userMapper,
                               OperationLogService logService) {
        this.farmerVerificationMapper = farmerVerificationMapper;
        this.userMapper = userMapper;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVerifications(Integer page, Integer pageSize) {
        List<FarmerVerification> all = farmerVerificationMapper.selectList(new LambdaQueryWrapper<FarmerVerification>()
                .orderByDesc(FarmerVerification::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public FarmerVerification review(Long id, boolean approved, String reason, String operator) {
        FarmerVerification verification = require(id);
        verification.setStatus(approved ? "approved" : "rejected");
        verification.setReason(reason);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        farmerVerificationMapper.updateById(verification);

        if (approved && verification.getUserId() != null) {
            User user = userMapper.selectById(verification.getUserId());
            if (user != null) {
                user.setRole("farmer");
                user.setStatus("active");
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        logService.log(operator, "review_farmer_verification", "审核农户认证 " + id + " -> " + verification.getStatus());
        return verification;
    }

    @Transactional
    public void batchReview(BatchReviewRequest request, String operator) {
        List<FarmerVerification> verifications = farmerVerificationMapper.selectBatchIds(request.ids());
        List<Long> userIds = verifications.stream().map(FarmerVerification::getUserId).filter(uid -> uid != null).collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        for (FarmerVerification verification : verifications) {
            verification.setStatus(request.approved() ? "approved" : "rejected");
            verification.setReason(request.reason());
            verification.setReviewedAt(LocalDateTime.now());
            verification.setUpdatedAt(LocalDateTime.now());
            farmerVerificationMapper.updateById(verification);

            if (request.approved() && verification.getUserId() != null) {
                User user = userMap.get(verification.getUserId());
                if (user != null) {
                    user.setRole("farmer");
                    user.setStatus("active");
                    user.setUpdatedAt(LocalDateTime.now());
                    userMapper.updateById(user);
                }
            }
        }
        logService.log(operator, "batch_review_farmers", "批量审核认证 " + request.ids().size() + " 条 -> " + (request.approved() ? "通过" : "驳回"));
    }

    private FarmerVerification require(Long id) {
        FarmerVerification verification = farmerVerificationMapper.selectById(id);
        if (verification == null) {
            throw new BizException(4047, "verification not found");
        }
        return verification;
    }
}
