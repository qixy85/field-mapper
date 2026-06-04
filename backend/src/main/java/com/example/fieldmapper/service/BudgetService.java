package com.example.fieldmapper.service;

import com.example.fieldmapper.model.BudgetItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Service
public class BudgetService {

    private final JdbcTemplate meloneJdbcTemplate;

    public BudgetService(JdbcTemplate meloneJdbcTemplate) {
        this.meloneJdbcTemplate = meloneJdbcTemplate;
    }

    private final RowMapper<BudgetItem> rowMapper = (rs, rowNum) -> {
        BudgetItem item = new BudgetItem();
        item.setId(rs.getLong("ID"));
        item.setDirection(rs.getString("DIRECTION"));
        item.setProject(rs.getString("PROJECT"));
        item.setArea(rs.getString("AREA"));
        item.setSpecificCost(rs.getString("SPECIFIC_COST"));
        item.setMonth(rs.getString("MONTH"));
        if (rs.getBigDecimal("ACTUAL_CUMULATIVE") != null)
            item.setActualCumulative(rs.getBigDecimal("ACTUAL_CUMULATIVE"));
        if (rs.getBigDecimal("BOOK_CUMULATIVE") != null)
            item.setBookCumulative(rs.getBigDecimal("BOOK_CUMULATIVE"));
        if (rs.getBigDecimal("DIFFERENCE") != null)
            item.setDifference(rs.getBigDecimal("DIFFERENCE"));
        item.setRemark(rs.getString("REMARK"));
        item.setVerifier(rs.getString("VERIFIER"));
        item.setCreatedBy(rs.getString("CREATED_BY"));
        if (rs.getTimestamp("CREATED_AT") != null)
            item.setCreatedAt(rs.getTimestamp("CREATED_AT").toLocalDateTime());
        if (rs.getTimestamp("UPDATED_AT") != null)
            item.setUpdatedAt(rs.getTimestamp("UPDATED_AT").toLocalDateTime());
        return item;
    };

    public List<BudgetItem> findAll() {
        return meloneJdbcTemplate.query("SELECT * FROM BUDGET_ITEMS ORDER BY ID", rowMapper);
    }

    public BudgetItem findById(Long id) {
        List<BudgetItem> list = meloneJdbcTemplate.query(
                "SELECT * FROM BUDGET_ITEMS WHERE ID = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(BudgetItem item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        meloneJdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO BUDGET_ITEMS (DIRECTION, PROJECT, AREA, SPECIFIC_COST, MONTH, " +
                    "ACTUAL_CUMULATIVE, BOOK_CUMULATIVE, DIFFERENCE, REMARK, VERIFIER, CREATED_BY, CREATED_AT, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getDirection());
            ps.setString(2, item.getProject());
            ps.setString(3, item.getArea());
            ps.setString(4, item.getSpecificCost());
            ps.setString(5, item.getMonth());
            ps.setBigDecimal(6, item.getActualCumulative());
            ps.setBigDecimal(7, item.getBookCumulative());
            ps.setBigDecimal(8, item.getDifference());
            ps.setString(9, item.getRemark());
            ps.setString(10, item.getVerifier());
            ps.setString(11, item.getCreatedBy());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(BudgetItem item) {
        meloneJdbcTemplate.update(
                "UPDATE BUDGET_ITEMS SET DIRECTION=?, PROJECT=?, AREA=?, SPECIFIC_COST=?, MONTH=?, " +
                "ACTUAL_CUMULATIVE=?, BOOK_CUMULATIVE=?, DIFFERENCE=?, REMARK=?, VERIFIER=?, UPDATED_AT=SYSTIMESTAMP " +
                "WHERE ID=?",
                item.getDirection(), item.getProject(), item.getArea(), item.getSpecificCost(), item.getMonth(),
                item.getActualCumulative(), item.getBookCumulative(), item.getDifference(),
                item.getRemark(), item.getVerifier(), item.getId());
    }
}
