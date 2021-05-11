package entities;

import genericmapper.Mapper;
import java.util.function.Function;

/**
 * Generated code. Do not edit, your changes will be lost.
 */
public class CompanyMapper extends Mapper<Company, String> {

    // No public ctor 
    private CompanyMapper() {
        super( Company.class, java.lang.invoke.MethodHandles.lookup()  );
    }

    // self register
    static {
        Mapper.register( new CompanyMapper() );
    }

    // the method that it is all about
    @Override
    public Object[] deconstruct(  Company c ) {
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
    public Function<Company, String> keyExtractor() {
        return ( Company c ) -> c.getTicker();
    }

    @Override
    public Class<String> keyType() {
        return String.class;

    }
}
