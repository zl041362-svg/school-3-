package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.mapper.OperationLogMapper;
import com.zhhs.nong.model.OperationLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Transactional
    public void log(String operator, String action, String detail) {
        OperationLog log = new OperationLog();
        log.setOperator(operator);
        log.setAction(action);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLogs(Integer page, Integer pageSize, String operator, String action, String dateFrom, String dateTo) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getId);

        if (StringUtils.hasText(operator)) {
            wrapper.eq(OperationLog::getOperator, operator);
        }
        if (StringUtils.hasText(action)) {
            wrapper.eq(OperationLog::getAction, action);
        }
        if (StringUtils.hasText(dateFrom)) {
            wrapper.ge(OperationLog::getCreatedAt, LocalDateTime.parse(dateFrom + "T00:00:00"));
        }
        if (StringUtils.hasText(dateTo)) {
            wrapper.le(OperationLog::getCreatedAt, LocalDateTime.parse(dateTo + "T23:59:59"));
        }

        List<OperationLog> all = operationLogMapper.selectList(wrapper);
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }
}
