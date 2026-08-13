import com.aishwarya.ers.dto.UserResponseDTO;
import com.aishwarya.ers.exception.UserNotFoundException;
import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private AutoCloseable closeable;
    private User testUser;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(101);
        testUser.setUsername("bob123");
        testUser.setDepartment("HR");
        testUser.setRole(Role.EMPLOYEE);
        testUser.setPasswordHash(BCrypt.hashpw("password123", BCrypt.gensalt()));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // ==========================================
    // 1. REGISTER TESTS
    // ==========================================
    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void register_Success() {
            User newUser = new User();
            newUser.setUsername("newUser1");
            newUser.setDepartment("HR");

            when(userRepository.findByUsername("newUser1")).thenReturn(null);
            when(userRepository.createUser(any(User.class))).thenReturn(true);

            UserResponseDTO result = userService.register(newUser, "password123");

            assertNotNull(result);
            assertEquals("newUser1", result.getUsername());
            assertEquals(Role.EMPLOYEE, newUser.getRole());
            assertNotNull(newUser.getPasswordHash());
            assertNotEquals("password123", newUser.getPasswordHash());
            verify(userRepository, times(1)).findByUsername("newUser1");
            verify(userRepository, times(1)).createUser(newUser);
        }

        @Test
        @DisplayName("Should throw RuntimeException when username is already taken")
        void register_UsernameTaken_ThrowsException() {
            User newUser = new User();
            newUser.setUsername(testUser.getUsername());

            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

            assertThrows(RuntimeException.class, () ->
                    userService.register(newUser, "password123")
            );

            verify(userRepository, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when repository fails to create user")
        void register_CreateFails_ThrowsException() {
            User newUser = new User();
            newUser.setUsername("newUser2");

            when(userRepository.findByUsername("newUser2")).thenReturn(null);
            when(userRepository.createUser(any(User.class))).thenReturn(false);

            assertThrows(RuntimeException.class, () ->
                    userService.register(newUser, "password123")
            );
        }
    }

    // ==========================================
    // 2. GET USER TESTS
    // ==========================================
    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should return user by ID when found")
        void getUserById_Success() {
            when(userRepository.findById(testUser.getId())).thenReturn(testUser);

            UserResponseDTO result = userService.getUserById(testUser.getId());

            assertNotNull(result);
            assertEquals(testUser.getUsername(), result.getUsername());
            verify(userRepository, times(1)).findById(testUser.getId());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when ID is not found")
        void getUserById_NotFound() {
            when(userRepository.findById(999)).thenReturn(null);

            assertThrows(UserNotFoundException.class, () -> userService.getUserById(999));

            verify(userRepository, times(1)).findById(999);
        }

        @Test
        @DisplayName("Should return user by username when found")
        void getUserByUsername_Success() {
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

            UserResponseDTO result = userService.getUserByUsername(testUser.getUsername());

            assertNotNull(result);
            assertEquals(testUser.getUsername(), result.getUsername());
            verify(userRepository, times(1)).findByUsername(testUser.getUsername());
        }

        @Test
        @DisplayName("Should throw RuntimeException when username is not found")
        void getUserByUsername_NotFound() {
            when(userRepository.findByUsername("ghost")).thenReturn(null);

            assertThrows(RuntimeException.class, () -> userService.getUserByUsername("ghost"));
        }

        @Test
        @DisplayName("Should return all users")
        void getAllUsers_Success() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<UserResponseDTO> results = userService.getAllUsers();

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(testUser.getUsername(), results.get(0).getUsername());
            verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return an empty list when there are no users")
        void getAllUsers_Empty() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponseDTO> results = userService.getAllUsers();

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    // ==========================================
    // 3. ROLE / DEPARTMENT TESTS
    // ==========================================
    @Nested
    @DisplayName("Role and Department Tests")
    class RoleDepartmentTests {

        @Test
        @DisplayName("Should return the user's role")
        void getRole_Success() {
            when(userRepository.findById(testUser.getId())).thenReturn(testUser);

            Role role = userService.getRole(testUser.getId());

            assertEquals(Role.EMPLOYEE, role);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when getting role for missing user")
        void getRole_NotFound() {
            when(userRepository.findById(999)).thenReturn(null);

            assertThrows(UserNotFoundException.class, () -> userService.getRole(999));
        }

        @Test
        @DisplayName("Should return the user's department")
        void getDepartment_Success() {
            when(userRepository.findById(testUser.getId())).thenReturn(testUser);

            String department = userService.getDepartment(testUser.getId());

            assertEquals("HR", department);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when getting department for missing user")
        void getDepartment_NotFound() {
            when(userRepository.findById(999)).thenReturn(null);

            assertThrows(UserNotFoundException.class, () -> userService.getDepartment(999));
        }
    }

    // ==========================================
    // 4. UPDATE USER TESTS
    // ==========================================
    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update department without changing password when none is given")
        void updateUser_DepartmentOnly_Success() {
            String originalHash = testUser.getPasswordHash();

            User updateFields = new User();
            updateFields.setDepartment("Finance");

            when(userRepository.findById(testUser.getId())).thenReturn(testUser);
            when(userRepository.update(any(User.class))).thenReturn(true);

            UserResponseDTO result = userService.updateUser(testUser.getId(), updateFields, null);

            assertNotNull(result);
            assertEquals("Finance", testUser.getDepartment());
            assertEquals(originalHash, testUser.getPasswordHash());
            verify(userRepository, times(1)).update(testUser);
        }

        @Test
        @DisplayName("Should update password when a new one is provided")
        void updateUser_WithNewPassword_Success() {
            String originalHash = testUser.getPasswordHash();

            User updateFields = new User();
            updateFields.setDepartment("Finance");

            when(userRepository.findById(testUser.getId())).thenReturn(testUser);
            when(userRepository.update(any(User.class))).thenReturn(true);

            userService.updateUser(testUser.getId(), updateFields, "newPassword456");

            assertNotEquals(originalHash, testUser.getPasswordHash());
            assertTrue(BCrypt.checkpw("newPassword456", testUser.getPasswordHash()));
        }

        @Test
        @DisplayName("Should throw RuntimeException when user does not exist")
        void updateUser_NotFound_ThrowsException() {
            User updateFields = new User();
            updateFields.setDepartment("Finance");

            when(userRepository.findById(999)).thenReturn(null);

            assertThrows(RuntimeException.class, () ->
                    userService.updateUser(999, updateFields, null)
            );

            verify(userRepository, never()).update(any(User.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when repository update fails")
        void updateUser_UpdateFails_ThrowsException() {
            User updateFields = new User();
            updateFields.setDepartment("Finance");

            when(userRepository.findById(testUser.getId())).thenReturn(testUser);
            when(userRepository.update(any(User.class))).thenReturn(false);

            assertThrows(RuntimeException.class, () ->
                    userService.updateUser(testUser.getId(), updateFields, null)
            );
        }
    }

    // ==========================================
    // 5. DELETE USER TESTS
    // ==========================================
    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should successfully delete an existing user")
        void deleteUser_Success() {
            when(userRepository.findById(testUser.getId())).thenReturn(testUser);
            when(userRepository.delete(testUser.getId())).thenReturn(true);

            assertDoesNotThrow(() -> userService.deleteUser(testUser.getId()));

            verify(userRepository, times(1)).delete(testUser.getId());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when deleting a missing user")
        void deleteUser_NotFound_ThrowsException() {
            when(userRepository.findById(999)).thenReturn(null);

            assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999));

            verify(userRepository, never()).delete(anyInt());
        }

        @Test
        @DisplayName("Should throw RuntimeException when repository fails to delete")
        void deleteUser_DeleteFails_ThrowsException() {
            when(userRepository.findById(testUser.getId())).thenReturn(testUser);
            when(userRepository.delete(testUser.getId())).thenReturn(false);

            assertThrows(RuntimeException.class, () -> userService.deleteUser(testUser.getId()));
        }
    }

    // ==========================================
    // 6. LOGIN TESTS
    // ==========================================
    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully log in with correct credentials")
        void login_Success() {
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

            UserResponseDTO result = userService.login(testUser.getUsername(), "password123");

            assertNotNull(result);
            assertEquals(testUser.getUsername(), result.getUsername());
        }

        @Test
        @DisplayName("Should throw RuntimeException when username does not exist")
        void login_UnknownUsername_ThrowsException() {
            when(userRepository.findByUsername("ghost")).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userService.login("ghost", "password123")
            );
            assertEquals("Invalid username or password", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw RuntimeException when password is incorrect")
        void login_WrongPassword_ThrowsException() {
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userService.login(testUser.getUsername(), "wrongPassword")
            );
            assertEquals("Invalid username or password", ex.getMessage());
        }
    }
}