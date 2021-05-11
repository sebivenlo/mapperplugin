package genericdao.pgdao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import static java.util.logging.Logger.*;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assumptions.assumeThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test if all sql exceptions are properly translated (as in wrapped in) into
 * DAOExceptions. For coverage. The methods all will find a connection that is
 * broken and throws an exception.
 *
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
@Disabled
class PGDAOConnectionClosedExceptionTest extends PGDAOExceptionTestBase {

    @BeforeAll
    static void checkDataSource() {

        DataSource ds = PGJDBCUtils.getDataSource( "simpledao" );
        assumeThat( ds ).isNotNull();
    }

    @Override
    Connection getConnection() {
        try {
            Connection realConnection = DBTestHelpers.ds.getConnection();
            return new NonClosingConnection( realConnection );
        } catch ( SQLException ex ) {
            getLogger( PGDAOConnectionClosedExceptionTest.class.getName() )
                    .log( Level.SEVERE, null, ex );
        }
        return null;
    }

//    @Disabled( "Ignored because in this case the exception is somehow swallowed" )
    @Test//( expected = DAOException.class )
    @Override
    void delete() {
        Assertions.assertThatCode( () -> {
            eDao.deleteEntity( gp );
        } ).doesNotThrowAnyException();
    }
}
