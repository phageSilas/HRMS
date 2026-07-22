package com.hrms.business.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.attendance.entity.AttendanceRecordEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤记录 Mapper。
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecordEntity> {

    /**
     * 按员工和日期查询打卡记录。
     *
     * @param employeeId 员工ID
     * @param recordDate 打卡日期
     * @return 打卡记录
     * 本方法使用的工具类: 无
     */
    @Select("""
            SELECT id, employee_id, group_id, record_date, clock_in_time, clock_out_time,
                   clock_in_status, clock_out_status, clock_in_ip, clock_out_ip,
                   clock_in_gps, clock_out_gps, device_info, correction_status,
                   create_time, update_time
            FROM hr_attendance_record
            WHERE employee_id = #{employeeId}
              AND record_date = #{recordDate}
            LIMIT 1
            """)
    AttendanceRecordEntity selectByEmployeeAndDate(@Param("employeeId") Long employeeId,
                                                   @Param("recordDate") LocalDate recordDate);

    /**
     * 插入上班打卡记录。
     *
     * @param entity 打卡记录
     * @return 影响行数
     * 本方法使用的工具类: 无
     */
    @Insert("""
            INSERT INTO hr_attendance_record
            (employee_id, group_id, record_date, clock_in_time, clock_in_status,
             clock_in_ip, clock_in_gps, device_info, correction_status, create_time, update_time)
            VALUES
            (#{employeeId}, #{groupId}, #{recordDate}, #{clockInTime}, #{clockInStatus},
             #{clockInIp}, #{clockInGps}, #{deviceInfo}, #{correctionStatus}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertClockIn(AttendanceRecordEntity entity);

    /**
     * 插入下班打卡记录。
     *
     * @param entity 打卡记录
     * @return 影响行数
     * 本方法使用的工具类: 无
     */
    @Insert("""
            INSERT INTO hr_attendance_record
            (employee_id, group_id, record_date, clock_out_time, clock_out_status,
             clock_out_ip, clock_out_gps, device_info, correction_status, create_time, update_time)
            VALUES
            (#{employeeId}, #{groupId}, #{recordDate}, #{clockOutTime}, #{clockOutStatus},
             #{clockOutIp}, #{clockOutGps}, #{deviceInfo}, #{correctionStatus}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertClockOut(AttendanceRecordEntity entity);

    /**
     * 更新上班打卡字段。
     *
     * @param entity 打卡记录
     * @return 影响行数
     * 本方法使用的工具类: 无
     */
    @Update("""
            UPDATE hr_attendance_record
            SET clock_in_time = #{clockInTime},
                clock_in_status = #{clockInStatus},
                clock_in_ip = #{clockInIp},
                clock_in_gps = #{clockInGps},
                device_info = #{deviceInfo},
                update_time = #{updateTime}
            WHERE id = #{id}
              AND clock_in_time IS NULL
            """)
    int updateClockIn(AttendanceRecordEntity entity);

    /**
     * 更新下班打卡字段。
     *
     * @param entity 打卡记录
     * @return 影响行数
     * 本方法使用的工具类: 无
     */
    @Update("""
            UPDATE hr_attendance_record
            SET clock_out_time = #{clockOutTime},
                clock_out_status = #{clockOutStatus},
                clock_out_ip = #{clockOutIp},
                clock_out_gps = #{clockOutGps},
                device_info = #{deviceInfo},
                update_time = #{updateTime}
            WHERE id = #{id}
              AND clock_out_time IS NULL
            """)
    int updateClockOut(AttendanceRecordEntity entity);

    /**
     * 查询员工日期范围内的打卡记录。
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 打卡记录列表
     * 本方法使用的工具类: 无
     */
    @Select("""
            SELECT id, employee_id, group_id, record_date, clock_in_time, clock_out_time,
                   clock_in_status, clock_out_status, clock_in_ip, clock_out_ip,
                   clock_in_gps, clock_out_gps, device_info, correction_status,
                   create_time, update_time
            FROM hr_attendance_record
            WHERE employee_id = #{employeeId}
              AND record_date BETWEEN #{startDate} AND #{endDate}
            ORDER BY record_date ASC
            """)
    List<AttendanceRecordEntity> selectByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    /**
     * 查询考勤组日期范围内指定员工的打卡记录。
     *
     * @param groupId     考勤组ID
     * @param employeeIds 员工ID列表
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 打卡记录列表
     * 本方法使用的工具类: 无
     */
    @Select("""
            <script>
            SELECT id, employee_id, group_id, record_date, clock_in_time, clock_out_time,
                   clock_in_status, clock_out_status, clock_in_ip, clock_out_ip,
                   clock_in_gps, clock_out_gps, device_info, correction_status,
                   create_time, update_time
            FROM hr_attendance_record
            WHERE group_id = #{groupId}
              AND record_date BETWEEN #{startDate} AND #{endDate}
              AND employee_id IN
              <foreach collection="employeeIds" item="employeeId" open="(" separator="," close=")">
                  #{employeeId}
              </foreach>
            ORDER BY record_date DESC, employee_id ASC
            </script>
            """)
    List<AttendanceRecordEntity> selectByGroupAndEmployeesAndDateRange(@Param("groupId") Long groupId,
                                                                       @Param("employeeIds") List<Long> employeeIds,
                                                                       @Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);

    /**
     * 更新补卡状态。
     *
     * @param id               打卡记录ID
     * @param correctionStatus 补卡状态
     * @return 影响行数
     * 本方法使用的工具类: 无
     */
    @Update("""
            UPDATE hr_attendance_record
            SET correction_status = #{correctionStatus}, update_time = NOW()
            WHERE id = #{id}
            """)
    int updateCorrectionStatus(@Param("id") Long id, @Param("correctionStatus") String correctionStatus);

    /**
     * 统计指定考勤组的打卡记录数量。
     *
     * @param groupId 考勤组ID
     * @return 打卡记录数量
     * 本方法使用的工具类: 无
     */
    @Select("""
            SELECT COUNT(1)
            FROM hr_attendance_record
            WHERE group_id = #{groupId}
            """)
    long countByGroupId(@Param("groupId") Long groupId);

    /**
     * 批量按员工ID列表和日期范围查询打卡记录
     *
     * @param employeeIds 员工ID列表
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 打卡记录列表
     * 本方法使用的工具类：foreach(MyBatis XML)
     */
    @Select("""
            <script>
            SELECT id, employee_id, group_id, record_date, clock_in_time, clock_out_time,
                   clock_in_status, clock_out_status, clock_in_ip, clock_out_ip,
                   clock_in_gps, clock_out_gps, device_info, correction_status,
                   create_time, update_time
            FROM hr_attendance_record
            WHERE employee_id IN
            <foreach collection="employeeIds" item="eid" open="(" separator="," close=")">
                #{eid}
            </foreach>
              AND record_date BETWEEN #{startDate} AND #{endDate}
            ORDER BY employee_id ASC, record_date ASC
            </script>
            """)
    List<AttendanceRecordEntity> selectByEmployeeIdsAndDateRange(@Param("employeeIds") List<Long> employeeIds,
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);
}
