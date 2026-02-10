package com.increff.pos.test.dao;

import com.increff.pos.config.TestConfig;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.documents.UserPojo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MongoOperations mongoOperations;

    private UserPojo testUser1;
    private UserPojo testUser2;
    private UserPojo testUser3;

    @BeforeEach
    public void setUp() {
        // Clean up the database before each test
        mongoOperations.dropCollection(UserPojo.class);

        // Create test users
        testUser1 = new UserPojo();
        testUser1.setEmail("testuser1");
        // Set other required fields based on your UserPojo class
        // e.g., testUser1.setPassword("password123");
        // testUser1.setRole("USER");

        testUser2 = new UserPojo();
        testUser2.setEmail("testuser2");
        // Set other required fields

        testUser3 = new UserPojo();
        testUser3.setEmail("testuser3");
        // Set other required fields
    }

    @AfterEach
    public void tearDown() {
        // Clean up after each test
        mongoOperations.dropCollection(UserPojo.class);
    }

    @Test
    public void testSave() {
        // Act
        UserPojo savedUser = userDao.save(testUser1);

        // Assert
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals(testUser1.getEmail(), savedUser.getEmail());
    }

    @Test
    public void testFindById() {
        // Arrange
        UserPojo savedUser = userDao.save(testUser1);

        // Act
        Optional<UserPojo> foundUser = userDao.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    public void testFindById_NotFound() {
        // Act
        Optional<UserPojo> foundUser = userDao.findById("nonexistent-id");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testFindByUsername() {
        // Arrange
        userDao.save(testUser1);

        // Act
        UserPojo foundUser = userDao.findByEmail("testuser1");

        // Assert
        assertNotNull(foundUser);
        assertEquals(testUser1.getEmail(), foundUser.getEmail());
    }

    @Test
    public void testfindByEmail_NotFound() {
        // Act
        UserPojo foundUser = userDao.findByEmail("nonexistentuser");

        // Assert
        assertNull(foundUser);
    }

    @Test
    public void testFindAll() {
        // Arrange
        userDao.save(testUser1);
        userDao.save(testUser2);
        userDao.save(testUser3);

        // Act
        List<UserPojo> allUsers = userDao.findAll();

        // Assert
        assertNotNull(allUsers);
        assertEquals(3, allUsers.size());
    }

    @Test
    public void testFindAll_Empty() {
        // Act
        List<UserPojo> allUsers = userDao.findAll();

        // Assert
        assertNotNull(allUsers);
        assertTrue(allUsers.isEmpty());
    }

    @Test
    public void testFindAll_WithPagination() {
        // Arrange
        userDao.save(testUser1);
        userDao.save(testUser2);
        userDao.save(testUser3);

        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<UserPojo> page = userDao.findAll(pageable);

        // Assert
        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    public void testUpdate() {
        // Arrange
        UserPojo savedUser = userDao.save(testUser1);
        String originalUsername = savedUser.getEmail();

        // Act
        savedUser.setEmail("updatedUsername");
        UserPojo updatedUser = userDao.save(savedUser);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("updatedUsername", updatedUser.getEmail());
        assertNotEquals(originalUsername, updatedUser.getEmail());

        // Verify in database
        Optional<UserPojo> foundUser = userDao.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("updatedUsername", foundUser.get().getEmail());
    }

    @Test
    public void testDelete() {
        // Arrange
        UserPojo savedUser = userDao.save(testUser1);
        String userId = savedUser.getId();

        // Act
        userDao.delete(savedUser);

        // Assert
        Optional<UserPojo> foundUser = userDao.findById(userId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testDeleteById() {
        // Arrange
        UserPojo savedUser = userDao.save(testUser1);
        String userId = savedUser.getId();

        // Act
        userDao.deleteById(userId);

        // Assert
        Optional<UserPojo> foundUser = userDao.findById(userId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testCount() {
        // Arrange
        userDao.save(testUser1);
        userDao.save(testUser2);

        // Act
        long count = userDao.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    public void testExistsById() {
        // Arrange
        UserPojo savedUser = userDao.save(testUser1);

        // Act & Assert
        assertTrue(userDao.existsById(savedUser.getId()));
        assertFalse(userDao.existsById("nonexistent-id"));
    }

    @Test
    public void testfindByEmail_MultipleSaves() {
        // Arrange
        userDao.save(testUser1);
        userDao.save(testUser2);

        // Act
        UserPojo found1 = userDao.findByEmail("testuser1");
        UserPojo found2 = userDao.findByEmail("testuser2");

        // Assert
        assertNotNull(found1);
        assertNotNull(found2);
        assertEquals("testuser1", found1.getEmail());
        assertEquals("testuser2", found2.getEmail());
        assertNotEquals(found1.getId(), found2.getId());
    }
}