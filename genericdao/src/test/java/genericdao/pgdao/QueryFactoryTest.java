package genericdao.pgdao;

import entities.Company;
import entities.Employee;
import genericmapper.Mapper;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import static java.util.stream.Collectors.joining;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Pieter van den Hombergh {@code Pieter.van.den.Hombergh@gmail.com}
 */
public class QueryFactoryTest {

    Mapper<?, ?> mapper = Mapper.mapperFor( Employee.class );
    QueryFactory fac = new QueryFactory( mapper );

    @Disabled("Think TDD")
    @Test
    void tSaveQueryText() {
        String queryText = cachedTest( () -> fac.saveQueryText() );
        //TODO
        fail( "tSaveQueryText completed succesfully; you know what to do" );
    }

    @Disabled("Think TDD")
    @Test
    void tDeleteQueryText() {
        String queryText = cachedTest( () -> fac.deleteQueryText() );
        //TODO
        fail( "tDeleteQueryText completed succesfully; you know what to do" );
    }

    @Disabled("Think TDD")
    @Test
    void tUpdateQueryText() {
        String queryText = cachedTest( () -> fac.updateQueryText() );
        //TODO
        fail( "tUpdateQueryText completed succesfully; you know what to do" );
    }

    @Disabled("Think TDD")
    @Test
    void tSelectQueryText() {
        String queryText = cachedTest( () -> fac.getQueryText() );
        //TODO
        fail( "tSelectQueryText completed succesfully; you know what to do" );
    }

    @Disabled("Think TDD")
    @Test
    void tSelectAllQueryText() {
        String queryText = cachedTest( () -> fac.allQuery() );
        //TODO
        fail( "tSelectAllQueryText completed succesfully; you know what to do" );
    }

    @Disabled("Think TDD")
    @Test
    void tLastIdQuery() {
        String queryText = cachedTest( () -> fac.lastIdQuery() );
        assertThat( queryText ).contains( "select", "max(employeeid)", "as",
                "lastid", "from", "employees" );

//        fail( "method LastIdQuery completed succesfully; you know what to do" );
    }

    @Disabled("Think TDD")
    @Test
    void tDropAllQuery() {
        String queryText = cachedTest( () -> fac.dropallQuery() );
        assertThat( queryText ).contains( "truncate", "employees", "restart",
                "identity", "cascade" );

//        fail( "method tDropAllQuery completed succesfully; you know what to do" );
    }

    /**
     * Check if a call result is cached.
     *
     * @param supplier
     * @return
     */
    String cachedTest( Supplier<String> supplier ) {
        String first = supplier.get();
        assertThat( supplier.get() ).isSameAs( first );
        return first;
    }

    @Disabled("Think TDD")
    @Test
    void tTableNameAnnotationUsed() {
        Mapper<?, ?> cmapper = Mapper.mapperFor( Company.class );
        QueryFactory fac = new QueryFactory( cmapper );

        assertThat( cachedTest( () -> fac.tableName() ) )
                .isEqualTo( "companies" );
//        fail( "method TableNameAnnotationUsed completed succesfully; you know what to do" );
    }
}
