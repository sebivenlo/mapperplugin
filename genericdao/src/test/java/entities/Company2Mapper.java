package entities;

import entities.Company2;
import genericmapper.Mapper;
import java.util.function.Function;

/**
 * Generated code. Do not edit, your changes will be lost.
 */
public class Company2Mapper extends Mapper<Company2, String> {

    // No public ctor 
    private Company2Mapper() {
        super( Company2.class, java.lang.invoke.MethodHandles.lookup()  );
    }

    // self register
    static {
        Mapper.register( new Company2Mapper() );
    }

    // the method that it is all about
    @Override
    public Object[] deconstruct(  Company2 c ) {
           return new Object[]{
                            c.getName(),
              c.getCountry(),
              c.getCity(),
              c.getAddress(),
              c.getTicker(),
              c.getPostcode(),
              c.getSomeInt(),
              c.getSomeInteger()
           }; 
    }

    @Override
    public Function<Company2, String> keyExtractor() {
        return ( Company2 c ) -> c.getTicker();
    }

    @Override
    public Class<String> keyType() {
        return String.class;

    }
}
