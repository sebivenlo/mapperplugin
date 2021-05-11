/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package genericdao.pgdao;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
 
/**
 *
 * @author Pieter van den Hombergh {@code Pieter.van.den.Hombergh@gmail.com}
 */
@Disabled("Think TDD")
public class PgJDBCUtilsTest {

  
    
    @Disabled("Think TDD")
    @Test
    void tNoProblemsIfApplicationPropsMissing() {
        assertThatCode(()->{
            PGJDBCUtils.getDataSource( "shouldNotexist" );
        }).doesNotThrowAnyException();
//        fail( "method NoProblemsIfApplicationPropsMissing completed succesfully; you know what to do" );
    }
}
