package entities;

import usertypes.Email;
import static genericdao.pgdao.DBTestHelpers.daof;
import static genericdao.pgdao.DBTestHelpers.loadDatabase;
import static usertypes.Email.email;
import genericdao.dao.DAO;
import genericdao.dao.DAOException;
import genericdao.pgdao.DBTestHelpers;
import genericdao.pgdao.PGDAOFactory;
import genericdao.pgdao.PGJDBCUtils;
import genericmapper.FieldPair;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.assumeThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
//@Ignore
//@FixMethodOrder( MethodSorters.NAME_ASCENDING )
@TestMethodOrder( MethodOrderer.MethodName.class )
public class EmployeeDaoTest {

    @BeforeAll
    public static void setupClass() {
        DataSource ds = PGJDBCUtils.getDataSource( "simpledao" );
        assumeThat( ds ).isNotNull();

        try {
            Class.forName( "entities.EmployeeMapper" );
            System.out.println( "loaded \"entities.EmployeeMapper\"" );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( EmployeeDaoTest.class.getName() )
                    .log( Level.SEVERE, ex.getMessage() );
        }
        loadDatabase();
        daof = new PGDAOFactory( ds ).registerInMarshaller( Email.class,
                Email::new );
        daof.registerPGMashallers( Email.class, Email::new, x -> PGDAOFactory
                .pgobject( "citext", x ) );
    }

    DAO<Employee, Integer> edao;

    @BeforeEach
    public void setupData() throws Exception {
        try {
            edao = daof.createDao( Employee.class );
        } catch ( Exception ex ) {
            Logger.getLogger( EmployeeDaoTest.class.getName() )
                    .log( Level.SEVERE, "***************************" + ex
                            .getMessage() );
        }
        assumeThat( edao ).isNotNull();
        DBTestHelpers.insertPiet();
        assertThat( edao.size() ).isEqualTo( 1 );
    }

    @AfterEach
    public void cleanup() {
    }

    @Test
    public void test00Size() {
        int size = edao.size();
        assertThat( size ).as( "tests start out with one element" )
                .isEqualTo( 1 );

        // fail( "test method test00Size reached its end, you ca remove this line when you aggree." );
    }

    @Test
    public void test01Get() {
//        DAO<Employee, Integer> edao = daof.createDao( Employee.class );
        int lastId = edao.lastId();
        Optional<Employee> e = edao.get( lastId );

        assertThat( e.isPresent() ).as( "got an employee" ).isTrue();
        assertThat( e.get().getFirstname() ).as( "It's Piet" )
                .isEqualTo( "Piet" );
        // fail( "testGet not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void test02GetAll() {
//        DAO<Employee, Integer> edao = daof.createDao( Employee.class );
        try {
            Collection<Employee> el = edao.getAll();
            assertThat( el.size() ).isEqualTo( 1 );
            assertThat( el.iterator().next().getFirstname() )
                    .isEqualTo( "Piet" );
        } catch ( NullPointerException npe ) {
            npe.printStackTrace();
        }
        //fail( "testGetAll not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void test03Delete() {
//        DAO<Employee, Integer> edao = daof.createDao( Employee.class );
        Employee dummy = new Employee( 1, null, null, null, null, null, null,
                null );
        edao.deleteEntity( dummy ); // will drop piet
        Optional<Employee> oe = edao.get( 1 );
        assertThat( oe ).isEmpty();

        // fail( "testDelete not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void test04Create() {
//        DAO<Employee, Integer> edao = daof.createDao( Employee.class );
        Collection<Employee> el = edao.getAll();
        int preSize = el.size();
        Employee savedJan = edao.save( JAN ).get();
        int postSize = edao.getAll().size();
        assertThat( postSize ).isEqualTo( 1 + preSize );
        edao.deleteEntity( savedJan );
        // fail( "testCreate not yet implemented. Review the code and comment or delete this line" );
    }
    private static final Employee JAN = new Employee( 0, "Klaassen", "Jan",
            email( "jan@example.com" ), 1, true, LocalDate.of( 1991, 2, 23 ),
            LocalDate.of( 1973, Month.MARCH, 4 ) );

    @Test
    public void test05Update() {
//        DAO<Employee, Integer> edao = daof.createDao( Employee.class );
        Employee savedJan = edao.save( JAN ).get();
        assertThat( savedJan ).isNotNull();
        assertThat( edao.getMapper().keyExtractor().apply( savedJan ) )
                .isNotEqualTo( 0 );
        System.out.println( "savedJan = " + savedJan );

        savedJan = edao.getMapper().replaceFields( savedJan, new FieldPair(
                "email",
                email( "janklaassen@outlook.com" ) ) );
        assertThat( savedJan ).isNotNull();
        edao.update( savedJan ); // ignore result for now
        Employee updatedJan = edao.get( savedJan.getEmployeeid() ).get();

        assertThat( savedJan.getEmail() ).extracting( e -> e.toString() )
                .isEqualTo( "janklaassen@outlook.com" );
        // fail( "test05Update not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void test00GetByKeyValues() {
//        DAO<Employee, Integer> edao = daof.createDao( Employee.class );
        // should get default piet.
        Employee savedJan = edao.save( JAN ).get();
        Collection<Employee> col = edao.getByColumnValues( "email", JAN.
                getEmail() );
        assertThat( col.isEmpty() ).isFalse();
        Employee firstEmployee = col.iterator().next();
        assertThat( firstEmployee.getFirstname() ).isEqualTo( "Jan" );
        edao.deleteEntity( savedJan );
        //fail( "test method testGetByKeyValues reached its end, you can remove this line when you aggree." );
    }

    @Test
    public void testSaveUniqueViolation() {
        Employee jean = new Employee( 0, "Klaassen", "Jean",
                email( "jan@example.com" ), 1, true, LocalDate.of( 1991, 2, 23 ),
                null );
//        DAO<Employee, Integer> edao = daof.createDao( Employee.class );
        // should get default piet.
        Employee savedJean = edao.save( jean ).get();
        assertThatThrownBy( () -> {
            Employee savedJan = edao.save( jean ).get();
        } ).isExactlyInstanceOf( DAOException.class );

        // fail( "testSaveUniqueViolation not yet implemented. Review the code and comment or delete this line" );
    }
}
