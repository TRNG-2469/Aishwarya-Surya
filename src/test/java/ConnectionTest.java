
import com.aishwarya.ers.util.ConnectionFactory;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

public class ConnectionTest {
    @Test
    public void getConnectionTest() {
        try (Connection connection = ConnectionFactory.getConnection()) {

            if (connection != null) {
                System.out.println("Connected to PostgreSQL successfully!");
            }

        } catch (Exception e) {
            e.getMessage();
        }

    }
}