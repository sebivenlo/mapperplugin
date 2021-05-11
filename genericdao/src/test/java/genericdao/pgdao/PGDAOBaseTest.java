package genericdao.pgdao;

import entities.Company;
import entities.Department;
import static usertypes.Email.email;
import entities.Employee;
import genericdao.dao.DAO;
import genericdao.dao.DAOException;
import genericdao.dao.TransactionToken;
import genericmapper.FieldPair;
import genericmapper.Mapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import org.junit.jupiter.api.Test;

/**
 * Run all base tests, avoid test run duplication.
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
public class PGDAOBaseTest extends PGDAOTestBase {

    @Test
    public void testExecuteInt0() {
        int id = eDao.executeIntQuery(
                "select employeeid from employees where firstname='Batman'" );
        assertThat( id ).isEqualTo( 0 );
        //fail( "test method testExceuteInt0 reached its end, you can remove this line when you aggree." );
    }

    @Test
    public void testSize() {
        int id = eDao.size();
        assertThat( id ).as( "there should be a piet" ).isEqualTo( 1 );
        //fail( "test method testExceuteInt0 reached its end, you can remove this line when you aggree." );
    }

    @Test//( expected = DAOException.class )
    public void testGetByColumnKeyValues() {
        Assertions.assertThatThrownBy( () -> {
            eDao.getByColumnValues( "batcar", "black" );
        } ).isExactlyInstanceOf( DAOException.class ); // column not in database should cause sql exception
        //fail( "test method testGetByColumnKeyValues reached its end, you can remove this line when you aggree." );
    }

    @Test
    public void testExecuteIntQueryInt1() {
        int id = eDao.executeIntQuery(
                "select departmentid from employees where employeeid=?", 1 );
        assertThat( id ).as( "piet is in dept 1" ).isEqualTo( 1 );
        // fail( "testExecuteIntQueryInt1 not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testGetConnection() {
        Connection connection = eDao.getConnection();
        assertThat( connection ).as( "having my connection" ).isNotNull();
        // fail( "testGetConnection not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testUsingTransactionConnection() throws SQLException {
        PGTransactionToken tok = new PGTransactionToken( ds.getConnection() );
        Connection tokCon = tok.getConnection();
        eDao.setTransactionToken( tok );
        Connection c = eDao.getConnection();
        assertThat( c ).as( "using one and the same connection" ).isSameAs(
                tokCon );
        assertThat( eDao.getTransactionToken() ).isSameAs( tok );
        //fail( "testUsingTransactionConnection not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testSaveAll() throws SQLException, Exception {
        LocalDate d = LocalDate.of( 1999, 5, 6 );
        LocalDate d2 = LocalDate.of( 1999, 2, 3 );
        Employee jan = new Employee( 0, "Klaassen", "Jan", email(
                "j.klaassen@fontys.nl" ), 1, true, d, LocalDate.now() );
        Employee kat = new Employee( 0, "Hansen", "Katrien", email(
                "j.hansen@fontys.nl" ), 1, false, d2, LocalDate.now() );
        System.out.println( "jan = " + jan );
        System.out.println( "kat = " + kat );
        DAO dao = daof.createDao( Employee.class );
        assumeThat( dao ).isNotNull();
        try ( final TransactionToken tok = dao.startTransaction() ) {
            Collection<Employee> saveAll = dao.saveAll( jan, kat );
            assertThat( saveAll.size() )
                    .as( "using one and the same connection" ).isEqualTo( 2 );
            // Assertions.assertThat( saveAll ).containsExactlyInAnyOrder( kat, jan );
            PGDAO pdao = (PGDAO) dao;
            assertThat( pdao.getConnection().isClosed() ).isFalse();
            Employee e1 = saveAll.iterator().next();
            Employee e2 = saveAll.iterator().next();
            dao.deleteAll( e1, e2 );
            tok.commit();
        }
        //Assert.fail( "test method testSaveAll reached its end, you can remove this line when you aggree." );
    }

//    @Disabled( "needs extra function" )
    @Test
    public void testNullableFields() {
        String tick = "BAS";
        Company c = new Company( null, null, null, null, tick, null );
        DAO<Company, String> cdao = daof.createDao( Company.class );
        Company sC = cdao.save( c ).get();
        cdao.deleteEntity( sC );
        assertSoftly( softly -> {
            softly.assertThat( sC.getTicker() ).isEqualTo( tick );
            softly.assertThat( sC.getName() ).as( "name is null" ).isNull();
            softly.assertThat( sC.getSomeInt() ).as( "someint" ).isEqualTo( 0 );
            softly.assertThat( sC.getSomeInteger() ).as( "someinteger" )
                    .isEqualTo( null );
        } );
        //Assert.fail( "test method testNullableFields reached its end, you can remove this line when you aggree." );
    }

//    @Disabled( "needs extra function" )
    @Test
    public void testDropGeneratedDept() {
        Mapper<Department, Integer> departmentMapper = Mapper.mapperFor(
                Department.class );
        daof.registerMapper( Department.class, departmentMapper );
        PGDAO<Department, String> ddao = daof.createDao( Department.class );
        Department e = new Department( "engineering", "The geniusses",
                "engineering@example.com,", null );
        Object[] parts = departmentMapper.deconstruct( e );
        assertThat( parts.length ).as( "part count" ).isEqualTo( 4 );
        Object[] nonGeneratedParts = new Object[ 0 ];
        List<FieldPair> remainingFields = ddao.dropGeneratedFields( e );
        assertThat( remainingFields.size() )
                .as( "normal field count" )
                .isEqualTo( 3 );
        //        Assert.fail( "test testDropGeneratedDept reached its end, you can remove me when you aggree." );
    }

//    @Disabled( "needs extra function" )
    @Test
    public void testDropGeneratedEmp() {
        Mapper<Employee, Integer> mapper = Mapper.mapperFor( Employee.class );
        LocalDate d = LocalDate.of( 1999, 5, 6 );
        daof.registerMapper( Employee.class, mapper );
        PGDAO<Employee, Integer> edao = daof.createDao( Employee.class );
        Employee jan = new Employee( 0, "Klaassen", "Jan",
                email( "j.klaassen@fontys.nl" ), 1, true, d, null );
        Object[] parts = mapper.deconstruct( jan );
        assertThat( parts.length ).as( "part count" ).isEqualTo( 8 );
        List<FieldPair> remainingFields = edao.dropGeneratedFields( jan );
        assertThat( remainingFields.size() ).as( "part count" ).isEqualTo( 6 );
        assertThat( remainingFields.get( 5 ).value() ).as( "last field is dob" )
                .isInstanceOf( LocalDate.class );
        //fail( "test testDropGeneratedDept reached its end, you can remove me when you aggree." );
    }

    @Test
    public void anyQuery() {
        Mapper<Employee, Integer> mapper = Mapper.mapperFor( Employee.class );
        PGDAO<Employee, Integer> eDaoa = daof.createDao( Employee.class );
        String sql = "select * from employees where employeeid = ?";
        List<Employee> list = eDaoa.anyQuery( sql, 1 );
        for ( Employee employee : list ) {
            System.out.println( "employee = " + employee );
        }
        assertThat( list ).hasSize( 1 );
        //fail( "method method reached end. You know what to do." );
    }

    @Disabled("Think TDD")
    @Test
    void tdropAllTest() {
        PGDAO<Employee, Integer> eDaoa = daof.createDao( Employee.class );
        assertThatCode( () -> {
            eDao.dropAll();
        } ).doesNotThrowAnyException();
//        fail( "method dropAllTest completed succesfully; you know what to do" );
    }
}
