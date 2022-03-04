package server.user.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import server.user.domain.Level;
import server.user.domain.User;
import server.user.sqlservice.SqlService;

import javax.sql.DataSource;
import java.util.List;

@Repository
@Service
public class UserDaoJdbc implements UserDao {
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private JdbcTemplate jdbcTemplate;

    private SqlService sqlService;

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    private RowMapper<User> userMapper =
            (rs, rowNum) -> {
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
            };

    public void add(User user) {
        this.jdbcTemplate.update(this.sqlService.getSql("userAdd"),
                user.getId(), user.getPassword(), user.getName(), user.getSex(), user.getAge(), user.getLevel().intValue(), user.getVisit(), user.getCash());
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject(this.sqlService.getSql("userGet"), this.userMapper, id);
    }

    public void deleteAll() {
        this.jdbcTemplate.update(this.sqlService.getSql("userDeleteAll"));
    }

    public int getCount()  {
        return this.jdbcTemplate.queryForObject(this.sqlService.getSql("userGetCount"), Integer.class);
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query(this.sqlService.getSql("userGetAll"),this.userMapper);
    }

    public void update(User user) {
        this.jdbcTemplate.update(this.sqlService.getSql(("userUpdate")),
                user.getPassword(), user.getName(), user.getSex(), user.getAge(), user.getLevel().intValue(), user.getVisit(), user.getCash(), user.getId());
    }
}