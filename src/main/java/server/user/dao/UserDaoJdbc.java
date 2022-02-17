package server.user.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import server.user.domain.Level;
import server.user.domain.User;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDaoJdbc implements UserDao {
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private JdbcTemplate jdbcTemplate;

    private RowMapper<User> userMapper =
            new RowMapper<User>() {
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setSex(rs.getString("sex"));
                    user.setAge(rs.getInt("Age"));
                    user.setLevel(Level.valueOf(rs.getInt("level")));
                    user.setVisit(rs.getInt("visit"));
                    user.setCash(rs.getInt("cash"));
                    return user;
                }
            };

    public void add(final User user) {
        this.jdbcTemplate.update("insert into users(id, password, name, sex, age, level, visit, cash) values(?,?,?,?,?,?,?,?)",
                user.getId(), user.getPassword(), user.getName(), user.getSex(), user.getAge(), user.getLevel(), user.getVisit(), user.getCash());
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject("select * from users where id = ?", this.userMapper, new Object[] {id});
    }

    public void deleteAll() {
        this.jdbcTemplate.update("delete from users");
    }

    public int getCount()  {
        return this.jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query("select * from users order by id",this.userMapper);
    }

    public void update(User user) {
        this.jdbcTemplate.update("update users set password = ?, name = ?, sex = ?, age = ?, level = ?, login = ?, visit = ?, cash = ?",
                user.getPassword(), user.getName(), user.getSex(), user.getAge(), user.getLevel(), user.getVisit(), user.getCash());
    }
}