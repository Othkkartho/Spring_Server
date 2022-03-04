package server.user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import server.user.dao.UserDao;
import server.user.domain.Level;
import server.user.domain.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static server.user.service.UserServiceImpl.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserServiceTest {
    @Autowired UserService userService;
    @Autowired UserService testUserService;
    @Autowired UserDao userDao;
    @Autowired MailSender mailSender;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired ApplicationContext context;

    List<User> users;

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("gilliam@gmail.com", "holden", "Gilliam Holden", "M", 22, Level.BRONZE, MIN_VISIT_FOR_SILVER-1, MIN_CASHSPEND_FOR_SILVER-1),
                new User("sutton@gmail.com", "joana", "Sutton Joana", "F", 23, Level.BRONZE, MIN_VISIT_FOR_SILVER, MIN_CASHSPEND_FOR_SILVER),
                new User("power@gmail.com", "rigoberto", "Power Rigoberto", "M", 24, Level.SILVER, MIN_VISIT_FOR_GOLD-1, MIN_CASHSPEND_FOR_GOLD-1),
                new User("blake@gmail.com", "julissa", "Black Julissa", "F", 25, Level.SILVER, MIN_VISIT_FOR_GOLD, MIN_CASHSPEND_FOR_GOLD),
                new User("ruiz@gmail.com", "zack", "Ruiz Zack", "M", 26, Level.GOLD, 15, 1500000)
        );
    }

    @Test
    public void upgradeLevels() {
       UserServiceImpl userServiceImpl = new UserServiceImpl();

       MockUserDao mockUserDao = new MockUserDao(this.users);
       userServiceImpl.setUserDao(mockUserDao);

       MockMailSender mockMailSender = new MockMailSender();
       userServiceImpl.setMailSender(mockMailSender);

       userServiceImpl.upgradeLevels();

       List<User> updated = mockUserDao.getUpdated();
       assertThat(updated.size()).isEqualTo(2);
       checkUserAndLevel(updated.get(0), "sutton@gmail.com", Level.SILVER);
       checkUserAndLevel(updated.get(1), "blake@gmail.com", Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size()).isEqualTo(2);
        assertThat(request.get(0)).isEqualTo(users.get(1).getId());
        assertThat(request.get(1)).isEqualTo(users.get(3).getId());
    }

    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertThat(updated.getId()).isEqualTo(expectedId);
        assertThat(updated.getLevel()).isEqualTo(expectedLevel);
    }

    static class MockUserDao implements UserDao {
        private List<User> users;
        private List<User> updated = new ArrayList<User>();

        private MockUserDao(List<User> users) {
            this.users = users;
        }
        public List<User> getUpdated() {
            return this.updated;
        }
        public List<User> getAll() {
            return this.users;
        }
        public void update(User user) {
            updated.add(user);
        }

        public void add(User user) {throw new UnsupportedOperationException();}
        public void deleteAll() {throw new UnsupportedOperationException();}
        public User get(String id) {throw new UnsupportedOperationException();}
        public int getCount() {throw new UnsupportedOperationException();}
    }

    static class MockMailSender implements MailSender {
        private List<String> requests = new ArrayList<String>();

        public List<String> getRequests() {
            return requests;
        }

        public void send(SimpleMailMessage mailMessage) throws MailException {
            requests.add(mailMessage.getTo()[0]);
        }

        public void send(SimpleMailMessage[] mailMessage) throws MailException {
        }
    }

    @Test
    public void mockUpgradeLevels() {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel()).isEqualTo(Level.SILVER);
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel()).isEqualTo(Level.GOLD);

        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0]).isEqualTo(users.get(1).getId());
        assertThat(mailMessages.get(1).getTo()[0]).isEqualTo(users.get(3).getId());
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel().nextLevel());
        }
        else {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel());
        }
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);	  // GOLD 레벨
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel()).isEqualTo(userWithLevel.getLevel());
        assertThat(userWithoutLevelRead.getLevel()).isEqualTo(Level.BRONZE);
    }

    @Test(expected = TransientDataAccessResourceException.class)
    public void readOnlyTransactionAttribute() {
        testUserService.getAll();
    }

    @Test
    public void upgradeAllOrNothing() {
        userDao.deleteAll();

        for(User user : users) userDao.add(user);

        try {
            testUserService.upgradeLevels();
//            fail("TestUserServiceException expected"); 에러 고쳐야함
        }
        catch(TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(0), false);
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    public void transactionSync() {
        userService.deleteAll();
        userService.add(users.get(0));
        userService.add(users.get(1));
    }

    public static class TestUserService extends UserServiceImpl {
        private String id = "power@gmail.com";

        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id))
                throw new TestUserServiceException();
            super.upgradeLevel(user);
        }

        public List<User> getAll() {
            for(User user : super.getAll()) {
                super.update(user);
            }
            return null;
        }
    }

    static class TestUserServiceException extends RuntimeException {
    }
}
