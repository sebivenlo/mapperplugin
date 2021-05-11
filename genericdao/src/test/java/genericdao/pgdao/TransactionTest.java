package genericdao.pgdao;

import entities.Department;
import entities.Employee;
import usertypes.Email;
import static usertypes.Email.email;
import genericdao.dao.DAO;
import genericdao.dao.TransactionToken;
import genericmapper.FieldPair;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
public class TransactionTest {

    static DataSource ds;
    static PGDAOFactory daof;

    @BeforeAll
    static void setupClass() throws IOException, SQLException {
        ds = PGJDBCUtils.getDataSource( "simpledao" );
        Assumptions.assumeThat( ds ).isNotNull();
        DBTestHelpers.loadDatabase();
        daof = new PGDAOFactory( ds );
//        daof.createDao(  Employee.class );
//        daof.registerMapper( Department.class, new DepartmentMapper() );
        daof.registerPGMashallers( Email.class, Email::new, x -> PGDAOFactory
                .pgobject( "citext", x ) );

    }

    DAO<Employee, Integer> checkEmpDao;
    DAO<Department, String> checkDepDao;

    @BeforeEach
    public void setup() {
        DBTestHelpers.loadDatabase();
        checkEmpDao = daof.createDao( Employee.class );
        checkDepDao = daof.createDao( Department.class );
    }

    @Test
    public void testRollback() {
        // first dao is consumed by transaction
        Employee henk = new Employee( 0, "Heijmans", "Henk",
                email( "henk@someclub.org" ), 1, false, LocalDate.of( 1955, 7,
                12 ), null );

        int beforeSize = checkEmpDao.size();
        try (
                DAO<Employee, Integer> edao = daof.createDao( Employee.class );
                TransactionToken tok = edao.startTransaction(); ) {
            Employee savedHenk = edao.save( henk ).get();
            assertThat( savedHenk.getEmployeeid() != 0 )
                    .as( "real employee id" ).isTrue();
            tok.rollback();
        } catch ( Exception ex ) {
            fail( "unexpected exception " + ex );
            Logger.getLogger( TransactionTest.class.getName() ).
                    log( Level.SEVERE, null, ex );
        }
        int afterSize = checkEmpDao.size();

        assertThat( beforeSize )
                .as( "size changed, although transaction rolled back " )
                .isEqualTo( afterSize );
        //fail( "testAddDeptWithBoss not yet implemented. Review the code and comment or delete this line" );
    }

    Employee dilbert = new Employee( 0, "O'Hana", "Dilbert",
            email( "dilbert@example.com" ), 1, true, LocalDate.of( 1980, 4,
            1 ), null );

    @Test
    public void testAddDeptWithBossRollBack() {
        Department engineering = new Department( "Engineering",
                "Where value creation happens", "dilbert@example.com", 0 );

        int deptSize = checkDepDao.size();
        int empSize = checkEmpDao.size();

        try (
                DAO<Department, String> ddao = daof
                        .createDao( Department.class );
                TransactionToken tok = ddao.startTransaction();
                DAO<Employee, Integer> edao = daof
                        .createDao( Employee.class, tok ); ) {
                    Department save = ddao.save( engineering ).get();
                    int depno = save.getDepartmentid();
                    edao.getMapper().replaceFields( dilbert, new FieldPair(
                            "departmentid", depno ) );
                    edao.save( dilbert );
                    int tempSize = edao.size();
                    assertThat( tempSize ).as( "temp emp size" )
                            .isEqualTo( empSize + 1 );
                    tok.rollback();
                } catch ( Exception ex ) {
                    fail( "unexpected exception " + ex );
                    Logger.getLogger( TransactionTest.class.getName() ).
                            log( Level.SEVERE, null, ex );
                }

                int newDepSize = checkDepDao.size();
                int newEmpSize = checkEmpDao.size();

                assertThat( newDepSize ).isEqualTo( deptSize );
                assertThat( newEmpSize ).isEqualTo( empSize );
//        fail( "testMethod not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testAddDeptWithBossCommit() {
        Department engineering = new Department( "Engineering",
                "Where value creation happens", "dilbert@example.com", null );
        int deptSize = checkDepDao.size();
        int empSize = checkEmpDao.size();
        Department savedDept = null;
        Employee savedDilbert = null;

        try (
                DAO< Department, String> ddao = daof
                        .createDao( Department.class );
                TransactionToken tok = ddao.startTransaction();
                DAO<Employee, Integer> edao = daof
                        .createDao( Employee.class, tok ); ) {
                    System.out.println( "tok = " + tok );
                    savedDept = ddao.save( engineering ).get();
                    Integer depno = savedDept.getDepartmentid();
                    Employee modDilbert
                            = edao.getMapper().replaceFields( dilbert,
                                    new FieldPair(
                                            "departmentid", depno ) );
                    savedDilbert
                            = edao.save( modDilbert ).get();
                    System.out.println( "savedDilbert = " + savedDilbert );
                    tok.commit();
                } catch ( Exception ex ) {
                    fail( "unexpected exception " + ex );
                    Logger.getLogger( TransactionTest.class.getName() ).
                            log( Level.SEVERE, null, ex );
                }

                int newDepSize = checkDepDao.size();
                System.out.println( "newDepSize = " + newDepSize );
                int newEmpSize = checkEmpDao.size();
                System.out.println( "newEmpSize = " + newEmpSize );

                // cleanup
                checkEmpDao.deleteEntity( savedDilbert );
                checkDepDao.deleteEntity( savedDept );
                assertThat( newDepSize == deptSize + 1
                        && newEmpSize == empSize + 1 )
                        .as( "both table should have grown" ).isTrue();
//        fail( "testAddDeptWithBossCommit not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testGetAll() {

        try (
                DAO<Employee, Integer> edao = daof.createDao( Employee.class ); ) {
            TransactionToken tok = edao.startTransaction();
            Collection<Employee> all = edao.getAll();
            Connection connection = ( (PGTransactionToken) tok ).getConnection();
            assertThat( connection.isClosed() )
                    .as( "connection should stay open" )
                    .isFalse();
            System.out.println( "all = " + all );
            int size = edao.size();
            System.out.println( "size = " + size );
            assertThat( all.size() ).as( "two calls same size" )
                    .isEqualTo( size );
            edao.close();
        } catch ( Exception ex ) {
            fail( "unexpected exception " + ex );
            Logger.getLogger( TransactionTest.class.getName() ).
                    log( Level.SEVERE, null, ex );
        }

        //fail( "testGetAll not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testGetPiet() throws SQLException {

        try ( DAO<Employee, Integer> edao = daof.createDao( Employee.class );
                TransactionToken tok = edao.startTransaction(); ) {
            Optional<Employee> optionalPiet = edao.get( 1 );

            assertThat( optionalPiet ).as( "do we have anything Piet" )
                    .isPresent();
            Connection connection = ( (PGTransactionToken) tok ).getConnection();
            assertThat( connection.isClosed() )
                    .as( "connection should stay open" )
                    .isFalse();
        } catch ( Exception ex ) {
            Logger.getLogger( TransactionTest.class.getName() ).
                    log( Level.SEVERE, null, ex );
            fail( "unexpected exception " + ex );
        }

    }

    Employee johnny = new Employee( 0, "Cash", "Johnny",
            email( "sue@nashville.town" ), 1, false, LocalDate
            .of( 1932, 02, 26 ), null );

    @Test
    public void testDelete() throws SQLException {

        try ( DAO<Employee, Integer> edao = daof.createDao( Employee.class );
                TransactionToken tok = edao.startTransaction(); ) {
            Employee savedJohnny = edao.save( johnny ).get();
            System.out.println( "Short appearance of Johnny: " + savedJohnny );
            edao.deleteEntity( savedJohnny );
            tok.commit();
        } catch ( Exception ex ) {
            fail( "unexpected exception " + ex );
            Logger.getLogger( TransactionTest.class.getName() ).
                    log( Level.SEVERE, null, ex );
        }

        //fail( "testDelete not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testUpdate() throws SQLException {
        //DAO<Employee,Integer> edao = daof.createDao( Employee.class );
        Employee johnnySaved = checkEmpDao.save( johnny ).get();
        Integer sid = johnnySaved.getEmployeeid();
        try ( DAO<Employee, Integer> edao = daof.createDao( Employee.class );
                TransactionToken tok = edao.startTransaction(); ) {
            Employee sj = edao.get( sid ).get();
            Employee sue = edao.getMapper().replaceFields( sj, new FieldPair(
                    "firstname", "Sue" ) );

            System.out.println( "Short appearance of Sue: " + sue );
            sue = edao.update( sue );
            Connection usedCon = ( (PGTransactionToken) tok ).getConnection();
            assertThat( usedCon.isClosed() )
                    .as( "connection still open?" )
                    .isFalse();
            System.out.println( "newJohn = " + sue );
            assertThat( sue.getFirstname() ).as( "last field is dob" )
                    .isEqualTo( "Sue" );
            tok.commit();
        } catch ( Exception ex ) {
            fail( "unexpected exception " + ex );
            Logger.getLogger( TransactionTest.class.getName() ).
                    log( Level.SEVERE, null, ex );
        }

        johnnySaved = checkEmpDao.get( sid ).get();
        assertThat( johnnySaved.getFirstname() ).isEqualTo( "Sue" );
        checkEmpDao.deleteEntity( johnnySaved );

        //fail( "testUpdate not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testGetByLastName() throws SQLException {
        //DAO<Employee,Integer> edao = daof.createDao( Employee.class );
        try ( DAO<Employee, Integer> edao = daof.createDao( Employee.class );
                TransactionToken tok = edao.startTransaction(); ) {
            Collection<Employee> allPuks = edao
                    .getByColumnValues( "lastname", "Puk" );
            assertThat( allPuks ).hasSize( 1 );
            Employee firstPuk = allPuks.iterator().next();

            assertThat( firstPuk.getFirstname() )
                    .as( "Hi again piet, going through a transaction" )
                    .isEqualTo( "Piet" );
        } catch ( Exception ex ) {
            fail( "unexpected exception " + ex );
            Logger.getLogger( TransactionTest.class.getName() ).
                    log( Level.SEVERE, null, ex );
        }

        //fail( "testGetByLastName not yet implemented. Review the code and comment or delete this line" );
    }
}
