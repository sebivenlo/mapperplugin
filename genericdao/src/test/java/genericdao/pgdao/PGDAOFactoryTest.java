package genericdao.pgdao;

import java.sql.Date;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
@Disabled
public class PGDAOFactoryTest {

    @Test
    public void marshallLocalDate() {
        LocalDate ld0 = LocalDate.now();
        Date d = Date.valueOf( ld0 );
        PGDAOFactory fac = new PGDAOFactory( null );

        LocalDate ld = fac.marshallIn( LocalDate.class, d );

        Date d2 = java.sql.Date.valueOf( ld );

        assertThat( d2 ).as( "date match" ).isEqualTo( d );
    }

}
