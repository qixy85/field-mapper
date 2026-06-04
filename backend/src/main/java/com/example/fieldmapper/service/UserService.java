package com.example.fieldmapper.service;

import com.example.fieldmapper.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class UserService {

    private final JdbcTemplate meloneJdbcTemplate;

    public UserService(JdbcTemplate meloneJdbcTemplate) {
        this.meloneJdbcTemplate = meloneJdbcTemplate;
    }

    private final RowMapper<AppUser> rowMapper = (rs, rowNum) -> {
        AppUser u = new AppUser();
        u.setId(rs.getLong("ID"));
        u.setUsername(rs.getString("USERNAME"));
        u.setPassword(rs.getString("PASSWORD"));
        u.setRole(rs.getString("ROLE"));
        if (rs.getTimestamp("CREATED_AT") != null)
            u.setCreatedAt(rs.getTimestamp("CREATED_AT").toLocalDateTime());
        u.setCreatedBy(rs.getString("CREATED_BY"));
        return u;
    };

    public AppUser findByUsername(String username) {
        List<AppUser> list = meloneJdbcTemplate.query(
                "SELECT * FROM APP_USERS WHERE USERNAME = ?", rowMapper, username);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<AppUser> findAll() {
        return meloneJdbcTemplate.query("SELECT * FROM APP_USERS ORDER BY ID", rowMapper);
    }

    public void createUser(String username, String password, String role, String createdBy) {
        meloneJdbcTemplate.update(
                "INSERT INTO APP_USERS (USERNAME, PASSWORD, ROLE, CREATED_AT, CREATED_BY) VALUES (?, ?, ?, SYSTIMESTAMP, ?)",
                username, password, role, createdBy);
    }
}
