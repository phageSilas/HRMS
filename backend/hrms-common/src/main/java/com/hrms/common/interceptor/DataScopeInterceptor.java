package com.hrms.common.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.hrms.common.annotation.DataScope;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据权限拦截器
 * <p>
 * 根据用户的数据权限范围，自动在 SQL 查询中注入过滤条件
 * </p>
 */
@Slf4j
@Component
public class DataScopeInterceptor implements InnerInterceptor {

    /**
     * 数据权限范围：1=仅本人，2=本部门，3=本部门及下属，4=全部
     */
    private static final int DATA_SCOPE_SELF = 1;
    private static final int DATA_SCOPE_DEPT = 2;
    private static final int DATA_SCOPE_DEPT_AND_SUB = 3;
    private static final int DATA_SCOPE_ALL = 4;

    private SqlSessionFactory sqlSessionFactory;

    @Lazy
    @Autowired
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                           ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 只处理 SELECT 语句
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }

        // 获取方法上的 @DataScope 注解
        DataScope dataScope = getDataScopeAnnotation(ms);
        if (dataScope == null) {
            return;
        }

        // 获取当前用户上下文
        UserContext userContext = SecurityContextHolder.getContext();
        if (userContext == null) {
            return;
        }

        // 超级管理员跳过数据权限过滤
        if (isAdmin(userContext)) {
            return;
        }

        // 获取用户的数据权限范围
        int dataScopeValue = getUserDataScope(userContext);

        // 根据数据权限范围注入 SQL 过滤条件
        String originalSql = boundSql.getSql();
        String newSql = injectDataScopeCondition(originalSql, dataScope, userContext, dataScopeValue);

        if (!originalSql.equals(newSql)) {
            log.debug("数据权限 SQL 拦截: {} -> {}", originalSql, newSql);
            // 修改 SQL
            BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), newSql, boundSql.getParameterMappings(),
                    boundSql.getParameterObject());
            // 复制额外参数
            boundSql.getAdditionalParameters().forEach(newBoundSql::setAdditionalParameter);
        }
    }

    /**
     * 获取方法上的 @DataScope 注解
     */
    private DataScope getDataScopeAnnotation(MappedStatement ms) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            Class<?> clazz = Class.forName(className);
            for (java.lang.reflect.Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(DataScope.class)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (Exception e) {
            log.error("获取 @DataScope 注解失败", e);
        }
        return null;
    }

    /**
     * 判断是否为超级管理员
     */
    private boolean isAdmin(UserContext userContext) {
        List<String> roleCodes = userContext.getRoleCodes();
        if (roleCodes != null) {
            return roleCodes.contains("ADMIN") || roleCodes.contains("ROLE_ADMIN");
        }
        return false;
    }

    /**
     * 获取用户的数据权限范围
     * <p>
     * 从用户角色中查询最小的 data_scope 值（权限范围最小=最严格）
     * </p>
     */
    private int getUserDataScope(UserContext userContext) {
        List<Long> roleIds = userContext.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            return DATA_SCOPE_SELF; // 默认仅本人
        }

        try {
            // 使用 SqlSession 直接查询角色的 data_scope
            // 取最小值（最严格的权限）
            String sql = "SELECT MIN(data_scope) FROM sys_role WHERE id IN " +
                         "(" + String.join(",", java.util.Collections.nCopies(roleIds.size(), "?")) + ")" +
                         " AND status = 1 AND is_deleted = 0";

            var session = sqlSessionFactory.openSession();
            try {
                var statement = session.getConnection().prepareStatement(sql);
                for (int i = 0; i < roleIds.size(); i++) {
                    statement.setLong(i + 1, roleIds.get(i));
                }
                var rs = statement.executeQuery();
                if (rs.next()) {
                    int dataScope = rs.getInt(1);
                    return dataScope > 0 ? dataScope : DATA_SCOPE_SELF;
                }
            } finally {
                session.close();
            }
        } catch (Exception e) {
            log.error("获取用户数据权限范围失败", e);
        }

        return DATA_SCOPE_SELF; // 默认仅本人
    }

    /**
     * 注入数据权限条件
     */
    private String injectDataScopeCondition(String sql, DataScope dataScope, UserContext userContext, int dataScopeValue) {
        try {
            Select select = (Select) net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            // 获取表名
            Table table = (Table) plainSelect.getFromItem();
            String alias = table.getAlias() != null ? table.getAlias().getName() : table.getName();

            Expression where = plainSelect.getWhere();
            Expression dataScopeCondition = null;

            switch (dataScopeValue) {
                case DATA_SCOPE_SELF:
                    // 仅本人：create_by = userId
                    EqualsTo selfCondition = new EqualsTo();
                    selfCondition.setLeftExpression(new Column(alias + "." + dataScope.createByColumn()));
                    selfCondition.setRightExpression(new LongValue(userContext.getUserId()));
                    dataScopeCondition = selfCondition;
                    break;
                case DATA_SCOPE_DEPT:
                    // 本部门：dept_id = deptId
                    EqualsTo deptCondition = new EqualsTo();
                    deptCondition.setLeftExpression(new Column(alias + "." + dataScope.deptColumn()));
                    deptCondition.setRightExpression(new LongValue(userContext.getDeptId()));
                    dataScopeCondition = deptCondition;
                    break;
                case DATA_SCOPE_DEPT_AND_SUB:
                    // 本部门及下属：dept_id IN (递归子部门)
                    List<Long> subDeptIds = getSubDeptIds(userContext.getDeptId());
                    InExpression inExpression = new InExpression();
                    inExpression.setLeftExpression(new Column(alias + "." + dataScope.deptColumn()));
                    // 构建 IN 表达式列表
                    net.sf.jsqlparser.expression.operators.relational.ExpressionList expressionList =
                        new net.sf.jsqlparser.expression.operators.relational.ExpressionList();
                    for (Long deptId : subDeptIds) {
                        expressionList.addExpressions(new LongValue(deptId));
                    }
                    inExpression.setRightExpression(expressionList);
                    dataScopeCondition = inExpression;
                    break;
                case DATA_SCOPE_ALL:
                default:
                    // 全部：不追加条件
                    return sql;
            }

            // 合并 WHERE 条件
            if (dataScopeCondition != null) {
                if (where == null) {
                    plainSelect.setWhere(dataScopeCondition);
                } else {
                    AndExpression andExpression = new AndExpression(where, dataScopeCondition);
                    plainSelect.setWhere(andExpression);
                }
            }

            return select.toString();
        } catch (Exception e) {
            log.error("注入数据权限条件失败", e);
            return sql;
        }
    }

    /**
     * 获取子部门ID列表（包含当前部门）
     * <p>
     * 使用递归 CTE 查询所有子部门
     * </p>
     */
    private List<Long> getSubDeptIds(Long deptId) {
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(deptId);

        if (deptId == null) {
            return deptIds;
        }

        try {
            // 使用递归 CTE 查询所有子部门
            String sql = "WITH RECURSIVE dept_tree AS (" +
                         "  SELECT id FROM sys_dept WHERE id = ? AND status = 1 AND is_deleted = 0" +
                         "  UNION ALL" +
                         "  SELECT d.id FROM sys_dept d" +
                         "  INNER JOIN dept_tree dt ON d.parent_id = dt.id" +
                         "  WHERE d.status = 1 AND d.is_deleted = 0" +
                         ") SELECT id FROM dept_tree";

            var session = sqlSessionFactory.openSession();
            try {
                var statement = session.getConnection().prepareStatement(sql);
                statement.setLong(1, deptId);
                var rs = statement.executeQuery();
                deptIds.clear();
                while (rs.next()) {
                    deptIds.add(rs.getLong("id"));
                }
            } finally {
                session.close();
            }
        } catch (Exception e) {
            log.error("获取子部门列表失败", e);
        }

        return deptIds;
    }

}
