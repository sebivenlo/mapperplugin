package genericdao.pgdao;

import genericdao.dao.DAOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * For coverage.
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
@Disabled
@ExtendWith(MockitoExtension.class)
public class PGTransactionTokenTest {

    @Mock
    Connection conn;

    @BeforeEach
    public void setup() throws SQLException {
//        MockitoAnnotations.initMocks( this );

        Mockito.doThrow( new SQLException( "Just for fun" ) ).when( conn )
                .setAutoCommit( false );
    }

    @Test
    public void testExceptionOnConnection() {
        Assertions.assertThatThrownBy( () -> {
            PGTransactionToken tok = new PGTransactionToken( conn );
        } ).isExactlyInstanceOf( DAOException.class );
    }

}
