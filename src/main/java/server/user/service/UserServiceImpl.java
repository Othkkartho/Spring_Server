package server.user.service;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import server.user.dao.UserDao;
import server.user.domain.Level;
import server.user.domain.User;

import java.util.List;

public class UserServiceImpl implements UserService {
    public static final int MIN_VISIT_FOR_SILVER = 5;
    public static final int MIN_CASHSPEND_FOR_SILVER = 500000;
    public static final int MIN_VISIT_FOR_GOLD = 10;
    public static final int MIN_CASHSPEND_FOR_GOLD = 1000000;

    private UserDao userDao;
    private MailSender mailSender;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void upgradeLevels() {
        List<User> users = userDao.getAll();
        for (User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
        }
    }
    private boolean canUpgradeLevel(User user) {
        Level currentLevel = user.getLevel();
        switch (currentLevel) {
            case BRONZE:
                return (user.getVisit() >= MIN_VISIT_FOR_SILVER && user.getCash() >= MIN_CASHSPEND_FOR_SILVER);
            case SILVER:
                return (user.getVisit() >= MIN_VISIT_FOR_GOLD && user.getCash() >= MIN_CASHSPEND_FOR_GOLD);
            case GOLD:
                return false;
            default:
                throw new IllegalArgumentException("Unknown Level: " + currentLevel);
        }
    }

    protected void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEMail(user);
    }

    private void sendUpgradeEMail(User user) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getId());
        mailMessage.setFrom("serveradmin@gmail.com");
        mailMessage.setSubject("Membership Update Information");
        mailMessage.setText(user.getName() + "'s membership information has been updated to " + user.getLevel().name());

        this.mailSender.send(mailMessage);
    }

    public void add(User user) {
        if (user.getLevel() == null)
            user.setLevel(Level.BRONZE);
        userDao.add(user);
    }

    public void deleteAll() {userDao.deleteAll();}
    public User get(String id) {return userDao.get(id);}
    public List<User> getAll() {return userDao.getAll();}
    public void update(User user) {userDao.update(user);}
}
