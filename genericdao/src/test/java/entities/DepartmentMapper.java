package entities;

import entities.Department;
import genericmapper.Mapper;
import java.util.function.Function;

/**
 * Generated code. Do not edit, your changes will be lost.
 */
public class DepartmentMapper extends Mapper<Department, String> {

    // No public ctor 
    private DepartmentMapper() {
        super( Department.class, java.lang.invoke.MethodHandles.lookup()  );
    }

    // self register
    static {
        Mapper.register( new DepartmentMapper() );
    }

    // the method that it is all about
    @Override
    public Object[] deconstruct(  Department d ) {
           return new Object[]{
                            d.getName(),
              d.getDescription(),
              d.getEmail(),
              d.getDepartmentid()
           }; 
    }

    @Override
    public Function<Department, String> keyExtractor() {
        return ( Department d ) -> d.getName();
    }

    @Override
    public Class<String> keyType() {
        return String.class;

    }
}
