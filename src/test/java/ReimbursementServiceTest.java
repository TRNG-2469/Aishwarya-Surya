import com.aishwarya.ers.exception.ReimbursementNotFoundException;
import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.ReimbursementType;
import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import com.aishwarya.ers.service.ReimbursementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReimbursementServiceTest {

    @Mock
    private ReimbursementRepository reimbursementRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReimbursementService reimbursementService;

    private AutoCloseable closeable;
    private User testUser;
    private User testManager;
    private Reimbursement sampleReimbursement;

    @BeforeEach
    void setUp() {
        // Initialize mocks programmatically without @ExtendWith
        closeable = MockitoAnnotations.openMocks(this);

        // Setup regular employee user
        testUser = new User();
        testUser.setId(101);
        testUser.setUsername("testuser1");
        testUser.setRole(Role.EMPLOYEE);

        // Setup manager user
        testManager = new User();
        testManager.setId(202);
        testManager.setUsername("testmanager1");
        testManager.setRole(Role.MANAGER);

        // Setup base reimbursement request
        sampleReimbursement = new Reimbursement();
        sampleReimbursement.setId(1);
        sampleReimbursement.setUserId(testUser.getId());
        sampleReimbursement.setAmount(new BigDecimal("42.00"));
        sampleReimbursement.setDescription("Client dinner");
        sampleReimbursement.setType(ReimbursementType.MEALS);
        sampleReimbursement.setStatus(ReimbursementStatus.PENDING);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // ==========================================
    // 1. SUBMIT REIMBURSEMENT TESTS
    // ==========================================
    @Nested
    @DisplayName("Submit Reimbursement Tests")
    class SubmitTests {

        @Test
        @DisplayName("Should successfully submit a valid reimbursement request")
        void submit_Success() {
            when(reimbursementRepository.create(any(Reimbursement.class))).thenReturn(true);

            Reimbursement submitted = reimbursementService.submit(sampleReimbursement);

            assertNotNull(submitted);
            assertEquals(ReimbursementStatus.PENDING, submitted.getStatus());
            assertEquals(new BigDecimal("42.00"), submitted.getAmount());
            verify(reimbursementRepository, times(1)).create(sampleReimbursement);
        }
    }

    // ==========================================
    // 2. GET REIMBURSEMENT TESTS
    // ==========================================
    @Nested
    @DisplayName("Get Reimbursement Tests")
    class GetTests {

        @Test
        @DisplayName("Should return reimbursement by ID when found")
        void getById_Success() {
            when(reimbursementRepository.findById(1)).thenReturn(sampleReimbursement);

            Reimbursement found = reimbursementService.getById(1);

            assertNotNull(found);
            assertEquals(1, found.getId());
            assertEquals("Client dinner", found.getDescription());
            verify(reimbursementRepository, times(1)).findById(1);
        }

        @Test
        @DisplayName("Should throw ReimbursementNotFoundException when reimbursement ID is not found")
        void getById_NotFound() {
            when(reimbursementRepository.findById(999)).thenReturn(null);

            assertThrows(
                    ReimbursementNotFoundException.class,
                    () -> reimbursementService.getById(999)
            );

            verify(reimbursementRepository, times(1)).findById(999);
        }

        @Test
        @DisplayName("Should return all reimbursements for a specific user")
        void getAllReimbursements_Success() {
            List<Reimbursement> mockList = List.of(sampleReimbursement);
            when(userRepository.findById(testUser.getId())).thenReturn(testUser);
            when(reimbursementRepository.findByUserId(testUser.getId())).thenReturn(mockList);

            List<Reimbursement> results = reimbursementService.getAllReimbursements(testUser.getId());

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(testUser.getId(), results.get(0).getUserId());
            verify(userRepository, times(1)).findById(testUser.getId());
            verify(reimbursementRepository, times(1)).findByUserId(testUser.getId());
        }
    }

    // ==========================================
    // 3. UPDATE PENDING REIMBURSEMENT TESTS
    // ==========================================
    @Nested
    @DisplayName("Update Pending Reimbursement Tests")
    class UpdatePendingTests {

        @Test
        @DisplayName("Should update description successfully when status is PENDING")
        void updatePending_Success() {
            sampleReimbursement.setDescription("Client dinner - updated");

            when(reimbursementRepository.updatePending(any(Reimbursement.class))).thenReturn(true);

            Reimbursement result = reimbursementService.updatePending(sampleReimbursement);

            assertNotNull(result);
            assertEquals("Client dinner - updated", result.getDescription());
            verify(reimbursementRepository, times(1)).updatePending(sampleReimbursement);
        }

        @Test
        @DisplayName("Should throw RuntimeException when trying to edit an APPROVED/RESOLVED reimbursement")
        void updatePending_ResolvedRequest_ThrowsException() {
            sampleReimbursement.setStatus(ReimbursementStatus.APPROVED);
            sampleReimbursement.setDescription("should fail");

            when(reimbursementRepository.findById(sampleReimbursement.getId())).thenReturn(sampleReimbursement);

            assertThrows(RuntimeException.class, () -> reimbursementService.updatePending(sampleReimbursement));
        }
    }

    // ==========================================
    // 4. APPROVE / RESOLVE TESTS
    // ==========================================
    @Nested
    @DisplayName("Approve Reimbursement Tests")
    class ApproveTests {

        @Test
        @DisplayName("Should successfully approve when user is a Manager")
        void approve_AsManager_Success() {
            when(userRepository.findById(testManager.getId())).thenReturn(testManager);
            when(reimbursementRepository.findById(sampleReimbursement.getId())).thenReturn(sampleReimbursement);

            when(reimbursementRepository.resolve(
                    eq(sampleReimbursement.getId()),
                    eq(ReimbursementStatus.APPROVED),
                    eq(testManager.getId())
            )).thenReturn(true);

            reimbursementService.approve(sampleReimbursement.getId(), testManager.getId());

            verify(userRepository, times(1)).findById(testManager.getId());
            verify(reimbursementRepository, times(1)).findById(sampleReimbursement.getId());
            verify(reimbursementRepository, times(1)).resolve(
                    sampleReimbursement.getId(),
                    ReimbursementStatus.APPROVED,
                    testManager.getId()
            );
        }

        @Test
        @DisplayName("Should throw RuntimeException when non-manager tries to approve")
        void approve_AsEmployee_ThrowsException() {
            when(userRepository.findById(testUser.getId())).thenReturn(testUser);

            assertThrows(RuntimeException.class, () ->
                    reimbursementService.approve(sampleReimbursement.getId(), testUser.getId())
            );

            verify(reimbursementRepository, never()).resolve(anyInt(), any(), anyInt());
        }
    }
}