package com.vaultcli.integration;

import com.vaultcli.config.TestDatabaseManager;
import com.vaultcli.dao.impl.PasswordEntryDaoImpl;
import com.vaultcli.dao.impl.UserDaoImpl;
import com.vaultcli.model.PasswordEntry;
import com.vaultcli.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {

    private static UserDaoImpl userDao;
    private static PasswordEntryDaoImpl passwordDao;

    @BeforeAll
    public static void setupDatabase() throws Exception {
        TestDatabaseManager.initialize("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");

        try (Connection conn = TestDatabaseManager.getTestConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS password_entries");
            stmt.execute("DROP TABLE IF EXISTS users");

            stmt.execute("CREATE TABLE users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE password_entries (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "service_name VARCHAR(255) NOT NULL, " +
                    "encrypted_password VARCHAR(255) NOT NULL, " +
                    "iv VARCHAR(255) NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id), " +
                    "UNIQUE(user_id, service_name))");
        }

        userDao = new UserDaoImpl();
        passwordDao = new PasswordEntryDaoImpl();
    }

    @Test
    public void userCreation() {
        User user = new User();
        user.setUsername("testUser");
        user.setPasswordHash("secureHash123");

        boolean created = userDao.createUser(user);
        assertTrue(created, "Uživatel by měl být vytvořen");
        assertNotEquals(0, user.getId(), "ID uživatele by mělo být přiřazeno po vytvoření");
    }

    @Test
    public void addPasswordEntry() {
        User user = createTestUser("passwordUser1");

        PasswordEntry entry = createPasswordEntry(user.getId(), "amazon", "encryptedPass1", "iv1");
        boolean added = passwordDao.addPasswordEntry(entry);
        assertTrue(added, "Heslo by mělo být úspěšně přidáno");
    }

    @Test
    public void getPasswordEntry() {
        User user = createTestUser("passwordUser2");
        passwordDao.addPasswordEntry(createPasswordEntry(user.getId(), "google", "encryptedPass2", "iv2"));

        PasswordEntry retrieved = passwordDao.getPasswordEntry(user.getId(), "google");
        assertNotNull(retrieved, "Heslo by mělo být načteno");
        assertEquals("google", retrieved.getServiceName(), "Název služby by měl odpovídat");
        assertEquals("encryptedPass2", retrieved.getEncryptedPassword(), "Zašifrované heslo by mělo odpovídat");
    }

    @Test
    public void updatePasswordEntry() {
        User user = createTestUser("passwordUser3");
        PasswordEntry entry = createPasswordEntry(user.getId(), "spotify", "oldEncrypted", "oldIv");
        passwordDao.addPasswordEntry(entry);

        PasswordEntry toUpdate = passwordDao.getPasswordEntry(user.getId(), "spotify");
        toUpdate.setEncryptedPassword("newEncrypted");
        toUpdate.setIv("newIv");

        boolean updated = passwordDao.updatePasswordEntry(toUpdate);
        assertTrue(updated, "Heslo by mělo být úspěšně aktualizováno");

        PasswordEntry updatedEntry = passwordDao.getPasswordEntry(user.getId(), "spotify");
        assertEquals("newEncrypted", updatedEntry.getEncryptedPassword(), "Zašifrované heslo by mělo být aktualizováno");
        assertEquals("newIv", updatedEntry.getIv(), "IV by mělo být aktualizováno");
    }

    @Test
    public void deletePasswordEntry() {
        User user = createTestUser("passwordUser4");
        passwordDao.addPasswordEntry(createPasswordEntry(user.getId(), "twitter", "encryptedPass3", "iv3"));

        boolean deleted = passwordDao.deletePasswordEntry(user.getId(), "twitter");
        assertTrue(deleted, "Heslo by mělo být úspěšně smazáno");

        PasswordEntry afterDeletion = passwordDao.getPasswordEntry(user.getId(), "twitter");
        assertNull(afterDeletion, "Smazané heslo by již nemělo existovat");
    }

    @Test
    public void getAllPasswordEntriesForUser() {
        User user = createTestUser("passwordUser5");

        passwordDao.addPasswordEntry(createPasswordEntry(user.getId(), "service1", "pass1", "iv1"));
        passwordDao.addPasswordEntry(createPasswordEntry(user.getId(), "service2", "pass2", "iv2"));
        passwordDao.addPasswordEntry(createPasswordEntry(user.getId(), "service3", "pass3", "iv3"));

        List<PasswordEntry> entries = passwordDao.getAllForUser(user.getId());
        assertEquals(3, entries.size(), "Mělo by být vráceno 3 hesla pro uživatele");
    }

    @Test
    public void getNonExistentPasswordEntry() {
        User user = createTestUser("passwordUser6");
        PasswordEntry entry = passwordDao.getPasswordEntry(user.getId(), "nonexistent");
        assertNull(entry, "Neexistující heslo by mělo vrátit null");
    }

    @Test
    public void getAllForUserWithNoEntries() {
        User user = createTestUser("passwordUser7");
        List<PasswordEntry> entries = passwordDao.getAllForUser(user.getId());
        assertTrue(entries.isEmpty(), "Pro uživatele bez hesel by měl být vrácen prázdný seznam");
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hashFor" + username);
        userDao.createUser(user);
        return user;
    }

    private PasswordEntry createPasswordEntry(int userId, String service, String password, String iv) {
        PasswordEntry entry = new PasswordEntry();
        entry.setUserId(userId);
        entry.setServiceName(service);
        entry.setEncryptedPassword(password);
        entry.setIv(iv);
        return entry;
    }
}
