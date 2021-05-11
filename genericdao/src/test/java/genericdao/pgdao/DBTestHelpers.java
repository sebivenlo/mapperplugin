package genericdao.pgdao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.joining;
import javax.sql.DataSource;
import static org.assertj.core.api.Assumptions.assumeThat;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
public class DBTestHelpers {

    public static DataSource ds = PGJDBCUtils.getDataSource( "simpledao" );
    public static PGDAOFactory daof;

    public static void loadDatabase() {
        try {
            String ddl
                    = Files.lines( Paths.get( "dbscripts/newpiet.sql" ) )
                            .filter( l -> !l.startsWith( "--" ) )
                            .collect( joining( System.lineSeparator() ) );
            doDDL( ddl );
        } catch ( IOException | SQLException ex ) {
            Logger.getLogger( DBTestHelpers.class.getName() ).
                    log( Level.SEVERE, null, ex );
        }
    }

    public static void insertPiet() {
        assumeThat( ds ).isNotNull();

        String truncate = "truncate employees restart identity cascade";
        String sql = " INSERT into employees (lastname,firstname,email,departmentid,available,dob)\n"
                + "values ('Puk','Piet','p.puk@vanderheiden.nl',1,true,'1993-03-17')";
        try ( Connection con = ds.getConnection();
                Statement stm = con.createStatement() ) {
            stm.execute( truncate );
            stm.execute( sql );

        } catch ( SQLException ex ) {
            Logger.getLogger( DBTestHelpers.class.getName() )
                    .log( Level.SEVERE, ex.getMessage() );
        }
    }

    public static void doDDL( String ddl ) throws
            SQLException {
        assumeThat( ds ).isNotNull();
        try ( Connection con = ds.getConnection();
                PreparedStatement pst = con.prepareStatement( ddl ); ) {
            System.out.println( "loading database" );
            pst.execute();
            System.out.println( "database ready for use" );
        }
    }

    @BeforeAll
    public static void setupClass() {
        assumeThat( ds ).isNotNull();
        daof = new PGDAOFactory( ds );
    }
}
