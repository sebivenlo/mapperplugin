package entities;

import entities.Truck;
import genericmapper.Mapper;
import java.util.function.Function;

/**
 * Generated code. Do not edit, your changes will be lost.
 */
public class TruckMapper extends Mapper<Truck, Integer> {

    // No public ctor 
    private TruckMapper() {
        super( Truck.class, java.lang.invoke.MethodHandles.lookup()  );
    }

    // self register
    static {
        Mapper.register( new TruckMapper() );
    }

    // the method that it is all about
    @Override
    public Object[] deconstruct(  Truck t ) {
           return new Object[]{
                            t.getTruckid(),
              t.getPlate()
           }; 
    }

    @Override
    public Function<Truck, Integer> keyExtractor() {
        return ( Truck t ) -> t.getTruckid();
    }

    @Override
    public Class<Integer> keyType() {
        return Integer.class;

    }
}
