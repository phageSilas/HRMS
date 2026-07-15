package com.hrms.business.attendance.mq;

import cn.hutool.json.JSONUtil;
import com.hrms.business.attendance.cache.AttendanceCacheKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 打卡成功事件处理入口，后续接入 RabbitMQ Consumer 后复用本类逻辑。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceClockEventHandler {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 处理打卡成功事件。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: JSONUtil(hutool),StringRedisTemplate(spring-data-redis),AttendanceCacheKeys(本模块cache包)
     */
    public void handleClockCreatedEvent(AttendanceClockCreatedEvent event) {
        String idempotentKey = AttendanceCacheKeys.clockMessageIdempotent(event.getMessageId());
        Boolean firstHandle = stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", Duration.ofHours(2));
        if (Boolean.FALSE.equals(firstHandle)) {
            return;
        }
        refreshDailyRecordCache(event);
        tempCheckAttendanceException(event);
    }

    /**
     * 刷新员工当天打卡状态缓存。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: JSONUtil(hutool),StringRedisTemplate(spring-data-redis),AttendanceCacheKeys(本模块cache包)
     */
    private void refreshDailyRecordCache(AttendanceClockCreatedEvent event) {
        String key = AttendanceCacheKeys.dailyRecord(event.getEmployeeId(), event.getRecordDate());
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(event), Duration.ofDays(2));
    }

    /**
     * 临时检查异常打卡并预留通知入口。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: 无
     */
    private void tempCheckAttendanceException(AttendanceClockCreatedEvent event) {
        if ("LATE".equals(event.getStatus()) || "EARLY_LEAVE".equals(event.getStatus())) {
            // notificationService.notifySupervisor(event); 本接口需要调用通知模块的主管/HR提醒方法。
            tempNotifyAttendanceException(event);
        }
    }

    /**
     * 临时通知考勤异常。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: 无
     */
    private void tempNotifyAttendanceException(AttendanceClockCreatedEvent event) {
        log.info("temp notify attendance exception: {}", event);
    }
}
