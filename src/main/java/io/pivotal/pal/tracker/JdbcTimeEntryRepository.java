package io.pivotal.pal.tracker;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private final JdbcTemplate jdbcTemplate;
    private RowMapper<TimeEntry> timeEntryRowMapper = (rs, rowNum) -> new TimeEntry(
            rs.getLong("id"),
            rs.getLong("project_id"),
            rs.getLong("user_id"),
            rs.getDate("date") == null ? null : rs.getDate("date").toLocalDate(),
            rs.getInt("hours")
    );

    public JdbcTimeEntryRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement("INSERT INTO time_entries (project_id, user_id, date, hours) " +
                            "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, timeEntry.getProjectId());
            ps.setLong(2, timeEntry.getUserId());
            ps.setDate(3, java.sql.Date.valueOf(timeEntry.getDate()));
            ps.setInt(4, timeEntry.getHours());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return buildNewTimeEntry(keyHolder.getKey().longValue(), timeEntry);
        }

        return null;
    }

    @Override
    public TimeEntry find(long timeEntryId) {
        List<TimeEntry> timeEntries = jdbcTemplate.query("select * from time_entries where id = " + timeEntryId, timeEntryRowMapper);
        return timeEntries.size() == 1 ? timeEntries.get(0) : null;
    }

    @Override
    public List list() {
        return jdbcTemplate.query("select * from time_entries", timeEntryRowMapper);
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        String sql = "UPDATE time_entries set project_id = ? ,user_id = ?, date = ?, hours = ? where id = ?";

        int updateRowCount = jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setLong(1, timeEntry.getProjectId());
            preparedStatement.setLong(2, timeEntry.getUserId());
            preparedStatement.setDate(3, java.sql.Date.valueOf(timeEntry.getDate()));
            preparedStatement.setInt(4, timeEntry.getHours());
            preparedStatement.setLong(5, id);
            return preparedStatement;
        });

        if (updateRowCount > 0) {
            return buildNewTimeEntry(id, timeEntry);
        }

        return null;
    }

    @Override
    public void delete(long timeEntryId) {
        jdbcTemplate.execute("delete from time_entries where id = " + timeEntryId);
    }

    private TimeEntry buildNewTimeEntry(long id, TimeEntry timeEntry) {
        return new TimeEntry(id,
                timeEntry.getProjectId(),
                timeEntry.getUserId(),
                timeEntry.getDate(),
                timeEntry.getHours());
    }
}
