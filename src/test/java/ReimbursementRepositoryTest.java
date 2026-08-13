import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.ReimbursementType;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.repository.ReimbursementRepository;
import com.aishwarya.ers.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class ReimbursementRepositoryTest {
    @Test
    public void main() {

        UserRepository userRepository = new UserRepository();
        ReimbursementRepository reimbursementRepository =
                new ReimbursementRepository();

        User user = userRepository.findByUsername("testuser1");

        if (user == null) {
            System.out.println("Test user was not found.");
            return;
        }

        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setUserId(user.getId());
        reimbursement.setAmount(new BigDecimal("75.50"));
        reimbursement.setDescription("Team lunch reimbursement");
        reimbursement.setType(ReimbursementType.MEALS);
        reimbursement.setStatus(ReimbursementStatus.PENDING);

        boolean created = reimbursementRepository.create(reimbursement);

        System.out.println("Reimbursement created: " + created);
        System.out.println("Generated reimbursement ID: " + reimbursement.getId());

        Reimbursement found =
                reimbursementRepository.findById(reimbursement.getId());

        if (found != null) {
            System.out.println("Found reimbursement:");
            System.out.println("ID: " + found.getId());
            System.out.println("User ID: " + found.getUserId());
            System.out.println("Amount: " + found.getAmount());
            System.out.println("Description: " + found.getDescription());
            System.out.println("Type: " + found.getType());
            System.out.println("Status: " + found.getStatus());
        } else {
            System.out.println("Reimbursement was not found.");
        }

        List<Reimbursement> userReimbursements =
                reimbursementRepository.findByUserId(user.getId());

        System.out.println(
                "Number of reimbursements for testuser1: "
                        + userReimbursements.size()
        );

    }
}
