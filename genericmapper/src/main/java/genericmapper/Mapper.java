package genericmapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import nl.fontys.sebivenlo.sebiannotations.Generated;
import nl.fontys.sebivenlo.sebiannotations.ID;

/**
 * Generic Mapper. Mapper functions:
 * <ul>
 * <li>Construct an entity from array of objects.</li>
 * <li>Deconstruct an entity into an array of objects.</li>
 * <li>Stream an entity as FieldPairs, which is pair of String name and Object
 * value<li>
 * </ul>
 *
 * @author Pieter van den Hombergh {@code p.vandenhombergh@fontys.nl}
 * @param <E> entity type to map.
 * @param <K> key for the entity
 */
public abstract class Mapper< E, K> {

    protected final Class<E> entityType;
    protected final Field[] entityFields;
    protected final Map<String, Integer> nameToFieldIndex;
    private final MethodHandles.Lookup lookup;
//    private final MethodHandles.Lookup lookup;
    private Set<String> generatedFields = null;

    private Constructor<E> declaredConstructor;
    protected final Function<Object[], E> allFieldsConstructor;

    /**
     * Create a mapper for given type.
     *
     * @param entityType type
     */
    protected Mapper( Class<E> entityType ) {
        this( entityType, MethodHandles.lookup() );
    }

    /**
     * Provide a lookup that gives access privileges to this Mapper in the
     * package of the subclass that invokes this constructor as super
     * constructor.
     *
     * @param entityType for this mapper
     * @param lookup     token to allow this mapper to do a method handle lookup
     *                   from the
     *                   package of the actual mapper.
     */
    protected Mapper( Class<E> entityType, MethodHandles.Lookup lookup ) {
        this.lookup = lookup;
        this.entityType = entityType;
        entityFields = entityFields( entityType );
        allFieldsConstructor = findConstructor( entityFields );
        nameToFieldIndex = nameToFieldIndex();
    }

    /**
     * Find all declared entityFields in the class hierarchy of the entity type
     * et. The stream only contains non static no synthetic fields.
     *
     */
    final Field[] entityFields( Class<E> et ) {
        List<Field[]> list = new ArrayList<>();
        Class<?> current = et;
        while ( current != Object.class ) {
            list.add( current.getDeclaredFields() );
            current = current.getSuperclass();
        }
        Collections.reverse( list );

        return list.stream()
                .flatMap( Stream::of )
                .filter( Constants::isNotStaticOrSynthetic )
                .toArray( Field[]::new );
    }

    /**
     * Returns unmodifiable list of the entity fields.
     *
     * @return the fields
     */
    public List<Field> entityFields() {
        return List.of( entityFields );
    }

    /**
     * Deconstruct the entity into an array of Objects.
     *
     * @param entity to deconstruct
     *
     * @return the entityFields in an array
     */
    public abstract Object[] deconstruct( E entity );

    /**
     * An entity typically has some kind of identity. Primary key in a database,
     * key in a HashMap.
     *
     * The function should produce a the key given an entity. You use it like
     * this: {@code K = mapper.keyExtractor(entity);}
     *
     * @return the extractor function.
     */
    public abstract Function<? super E, ? extends K> keyExtractor();

    /**
     * Return the key defined for the entity.
     *
     * @return class of entity
     */
    public abstract Class<?> keyType();

    /**
     * Create an object using the field values.
     *
     * @param fieldValues to use
     *
     * @return the constructed entity
     */
    public E construct( Object[] fieldValues ) {
        return allFieldsConstructor.apply( fieldValues );
    }

    /**
     * Stream the entity as field-name and field value pairs
     *
     * @param entity to stream
     *
     * @return Stream of field information
     */
    public Stream<FieldPair> stream( E entity ) {
        Object[] fieldValues = deconstruct( entity );
        return IntStream
                .range( 0, entityFields.length )
                .mapToObj( i -> new FieldPair( entityFields[ i ].getName(),
                fieldValues[ i ] )
                );
    }

    /**
     * Stream the non-generated or normal fields of the entity as pairs of name
     * and value.
     *
     * @param entity to stream
     *
     * @return the stream
     */
    public Stream<FieldPair> streamNonGenerated( E entity ) {
        return stream( entity )
                .filter( fp -> !this.generatedFieldNames().contains( fp.key() ) );
    }

    /**
     * Return the non-generated fields as a list of field pairs.
     *
     * @param entity to take apart
     *
     * @return the list
     */
    public List<FieldPair> nonGeneratedFieldPairs( E entity ) {
        return streamNonGenerated( entity ).collect( toList() );
    }

    /**
     * Helper to find the constructor using the entityFields definition.
     *
     * @param fields to find the signature of the constructor
     *
     * @return a function that can construct and entity form an array
     */
    final Function<Object[], E> findConstructor( Field[] fields ) {
        Function<Object[], E> assembler = null;
        try {
            Class[] fieldTypes = Stream.of( fields )
                    .map( f -> f.getType() )
                    .toArray( gen -> new Class[ fields.length ] );
            declaredConstructor = entityType.getConstructor( fieldTypes );
            MethodHandle ctorHandle = lookup.unreflectConstructor(
                    declaredConstructor );
            assembler = createCtorFunction( ctorHandle );
            Logger.getLogger( Mapper.class.getName() ).log( Level.INFO,
                    "found constructor for {0}", entityType.getName()
            );
        } catch ( IllegalAccessException | NoSuchMethodException ex ) {
            Supplier<String> puk = () -> String.format(
                    "failed to find constructor for class %s with exception type %s and message %3s",
                    entityType.getClass().getName(), ex.getClass().getName(),
                    ex.getMessage() );
            Logger.getLogger( Mapper.class.getName() )
                    .log( Level.SEVERE, puk );
        }
        return assembler;
    }

    /**
     * Use a method handle to create a function that can construct an entity
     * from an array of objects.
     *
     * @param ctorHandle method handle to a found candidate Constructor
     *
     * @return the function.
     */
    @SuppressWarnings( "unchecked" )
    private Function<Object[], E> createCtorFunction( MethodHandle ctorHandle ) {
        Function<Object[], E> assembler;
        assembler = ( Object[] a ) -> {
            E cresult = null;
            try {
                cresult = (E) ctorHandle.invokeWithArguments( a );
//                cresult = (E) declaredConstructor.newInstance( a );//ctorHandle.invokeWithArguments( a );
            } catch ( Throwable ex ) {
                Logger.getLogger( Mapper.class.getName() ).log( Level.SEVERE, ex
                        .getMessage() );
            }
            return cresult;
        };
        return assembler;
    }

    private static final ConcurrentMap<Class<?>, Mapper< ?, ?>> register = new ConcurrentHashMap<>();

    /**
     * Register a mapper.
     *
     * @param em mapper to register.
     */
    protected static void register( Mapper<?, ?> em ) {
        register.put( em.entityType, em );
    }

    /**
     * Creates an unmodifiable map of field name to field index.
     *
     * @return
     */
    final Map<String, Integer> nameToFieldIndex() {
        var result = new HashMap<String, Integer>();
        int idx = 0;
        for ( Field entityField : entityFields ) {
            result.put( entityField.getName(), idx++ );
        }

        return Map.copyOf( result );
    }

    /**
     * Return the size of the array created when deconstructing this mapper's
     * entitytype.
     *
     * @return the lengthn of the array.
     */
    public int getArraySize() {
        return entityFields.length;
    }

    static class StringMapper extends Mapper< String, String> {

        private StringMapper() {
            super( String.class, MethodHandles.lookup() );
        }

        static {
            register.put( String.class, new StringMapper() );
        }

        @Override
        public Object[] deconstruct( String entity ) {
            return new Object[]{ entity };
        }

        /**
         * simply return the string.
         *
         * @return the implementation for this extractor.
         */
        @Override
        public Function<String, String> keyExtractor() {
            return x -> x;
        }

        @Override
        public Class<?> keyType() {
            return String.class;

        }
    }

    static {
        System.out.println( "loading  StringMapper" );
        try {
            Class.forName( "genericmapper.Mapper$StringMapper" );
        } catch ( ClassNotFoundException neverhappens ) {
        }
    }

    /**
     * Retrieve a mapper for the given type. The mapper is either loaded by the
     * class loader or returned from a cache after the first load.
     *
     * @param <X> generic entity type
     * @param <Y> generic key field type of the entity
     * @param et  type of (class) entity
     *
     * @return the mapper.
     */
    @SuppressWarnings("unchecked")
    public static <X, Y> Mapper< X, Y> mapperFor(
            Class<X> et ) {
        if ( !register.containsKey( et ) ) {
            loadMapperClass( et );
        }
        return (Mapper< X, Y>) register.get( et );
    }

    /**
     * Try to load a mapper for an entity by name. If the type == String.class,
     * do nothing, because String is special.
     *
     * @param <E>       generic type of entity
     * @param forEntity class
     *
     * @throws a RuntimeException when the requested mapper class cannot be
     *           loaded
     */
    static <E> void loadMapperClass( Class<E> forEntity ) {
        if ( forEntity == String.class ) {
            return;
        }
        String mapperName = Constants.mapperName( forEntity );
        try {
            Class.forName( mapperName, true, forEntity.getClassLoader() );
            Logger.getLogger( Mapper.class.getName() )
                    .log( Level.INFO,
                            "mapper {0} for class {1} successfully loaded",
                            new Object[]{ mapperName, forEntity.getSimpleName() } );

        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( Mapper.class.getName() ).log( Level.SEVERE,
                    "could not find mapper {0} for class {1}",
                    new String[]{ mapperName, forEntity.getSimpleName() } );
            throw new RuntimeException( ex );
        }
    }

    private String idName = null;

    /**
     * Get the name of the key field.
     * The method tries to find the {@code @ID} annotation or returns id to
     * guess for a field name.
     *
     * @return the key field name.
     */
    public String getKeyFieldName() {
        if ( idName == null ) {
            idName = entityFields().stream()
                    .filter( f -> f.getAnnotation( ID.class ) != null )
                    .findFirst()
                    .map( f -> f.getName() )
                    .orElse( "id" );
        }
        return idName;
    }

    /**
     * Get the entity type mapped by this mapper.
     *
     * @return the type.
     */
    public Class<E> getEntityType() {
        return entityType;
    }

    /**
     * Replace the fields in the entities specified in the list with the
     * values in the list.USe the nameToFieldIndex map to find the position in
     * the array to modify.
     * {@code E pieternel = replaceFields(piet, List.of(new FieldPair("gender","F"),new FieldPair("firstname","Pieternel")));}
     *
     * @param e            entity
     * @param replacements key value pairs
     *
     * @return a new entity constructed from a deconstructed and field replaced
     *         instance.
     */
    public E replaceFields( E e, FieldPair... replacements ) {
        System.out.println( "e =" + e.toString() + "rep " + Arrays.toString(
                replacements ) );
        Object[] fields = deconstruct( e );
        if ( fields.length != entityFields.length ) {
            throw new RuntimeException(
                    "Can't find constructor for array size " + fields.length );
        }
        for ( FieldPair replacement : replacements ) {
            int idx = nameToFieldIndex.get( replacement.key() );
            fields[ idx ] = replacement.value();
        }
        return construct( fields );
    }

    private boolean isGenerated( Field f ) {
        ID idannotation = f.getAnnotation( ID.class );
        Generated genannotation = f.getAnnotation( Generated.class );

        return null != genannotation || ( null != idannotation && idannotation
                .generated() );
    }

    /**
     * Return unmodifiable set containing the names of the generated fields.
     *
     * This method lazily evaluates the generated fields. The result of this set
     * can be used to remove generated fields form the
     * {@code FieldPair stream(Entity)} to drop those and use the resulting
     * stream or list to populate a query.
     *
     * Hint: use Set.copyOf to create unmodifyable copy.
     *
     * @return the field names.
     */
    public final Set<String> generatedFieldNames() {
        if ( generatedFields == null ) {
            Field[] declaredFields = entityType.getDeclaredFields();

            Set<String> set = entityFields().stream()
                    .filter( this::isGenerated )
                    .map( Field::getName )
                    .collect( toSet() );
            generatedFields = Set.copyOf( set );
        }
        return generatedFields;
    }
}
