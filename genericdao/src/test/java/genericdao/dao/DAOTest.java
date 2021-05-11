package genericdao.dao;

import entities.Employee;
import genericmapper.FieldPair;
import genericmapper.Mapper;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static usertypes.Email.email;

/**
 * Coverage.
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
@Disabled
public class DAOTest {

    static class DummyDAO implements DAO<Employee, Integer> {

        final Mapper<Employee, Integer> mapper = Mapper.mapperFor(
                Employee.class );

        public DummyDAO() {

        }

        @Override
        public Optional<Employee> get( Integer id ) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<Employee> getAll() {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Optional<Employee> save( Employee e ) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Employee update( Employee e ) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void deleteEntity( Employee e ) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void deleteById( Integer k ) {
            throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Mapper getMapper() {
            return mapper;
        }

    };

    TransactionToken ignoredToken = new TransactionToken() {
    };

    DAO dao = new DummyDAO();

    /**
     * This test serves to cover the default implemenations in DAO.
     *
     * @throws Exception no expected
     */
    @Test
    public void testAllCovered() throws Exception {

        assertThat( dao.startTransaction() ).isNull();
        dao.setTransactionToken( ignoredToken );
        assertThat( dao.getTransactionToken() ).isNull();
        assertThat( dao.size() ).isEqualTo( 0 );
        assertThat( dao.lastId() ).isEqualTo( 0 );
        try {
            dao.close();
        } catch ( Exception e ) {
            fail( "should not throw exception, threw " + e );
        }
        //fail( "testAllCovered not yet implemented. Review the code and comment or delete this line" );
    }

    @Test
    public void testNotImplemented() {
        assertThatThrownBy( () -> {
            List notexepected = dao.getByColumnValues( "key", "value" );
        } ).isExactlyInstanceOf( UnsupportedOperationException.class );
        // fail( "test method testNotImplemented reached its end, you can remove this line when you aggree." );
    }

    @Disabled("Think TDD")
    @Test
    void tDropGeneratedFields() {
        Employee jean                = new Employee( 0, "Klaassen", "Jean",
                        email( "jan@example.com" ), 1, true, LocalDate.of( 1991,
                        2, 23 ),
                        LocalDate.of( 1973, Month.MARCH, 4 ) );
        dao.getMapper().deconstruct( jean );
        List<FieldPair> dropGeneratedFields = dao.dropGeneratedFields( jean );
        final List<String> remainingFields = dropGeneratedFields.stream()
                .map( FieldPair::key )
                .collect(
                        toList() );

        assertSoftly( softly -> {
            softly.assertThat( remainingFields ).as(
                    "id should have been dropped" )
                    .hasSize( 6 );
            softly.assertThat( remainingFields ).containsExactly( "lastname",
                    "firstname", "email", "departmentid", "available", "dob" );
        } );
//        fail( "method DropGeneratedFields completed succesfully; you know what to do" );
    }
}
