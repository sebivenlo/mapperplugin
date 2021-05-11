package genericmapper;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import testutils.TestUtils;

/**
 *
 * @author Pieter van den Hombergh {@code Pieter.van.den.Hombergh@gmail.com}
 */
public class FieldPairTest {

    @Disabled("Think TDD")
    @Test
    void tEqualsHashCodeToSatisfyCoverage() {
        Object o = new Object();
        Object o2 = new Object();
        FieldPair a = new FieldPair( "Hi", o );
        FieldPair a1 = new FieldPair( "Hi", o2 );
        FieldPair b = new FieldPair( "Bye", o );
        TestUtils.verifyEqualsAndHashCode( a, a1, b );
//        fail( "method EqualsHashCodeToSatisfyCoverage completed succesfully; you know what to do" );
    }
    
    @Disabled("Think TDD")
    @Test
    void tGetters() {
         FieldPair b = new FieldPair( "Hi", 1 );
         FieldPair b2 = new FieldPair( "Hi", null );
         
            SoftAssertions.assertSoftly( softly->{
                softly.assertThat(b.hasNullValue() ).isFalse();
                softly.assertThat(b2.hasNullValue() ).isTrue();
                softly.assertThat(b.key() ).isEqualTo("Hi");
                softly.assertThat(b.value() ).isEqualTo(1);
                softly.assertThat(b.toString()).isNotEmpty();
                softly.assertThat(b2.toString()).isNotEmpty();
            } );
         
//        fail( "method Getters completed succesfully; you know what to do" );
    }

}
