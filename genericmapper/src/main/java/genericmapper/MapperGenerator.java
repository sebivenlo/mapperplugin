package genericmapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import nl.fontys.sebivenlo.sebiannotations.ID;

/**
 * Generates mappers for named Types. The types are fully qualified types to be
 * read from the class path.
 *
 * @author Pieter van den Hombergh {@code Pieter.van.den.Hombergh@gmail.com}
 */
public class MapperGenerator {

    final Class<?> entityType;

    public MapperGenerator( Class<?> entitype ) {
        this.entityType = entitype;
    }

    /**
     * Generate the java code using the template MAPPER_TEMPLATE.
     *
     * @return the template text or an empty string when the class has no usable
     *         id field.
     */
    public final String javaSource() {
        String classText = "";
        //TODO generate the code
        return "";
    }

    /**
     * Turn the fields of a class into getter call strings.
     *
     * @param type to reflect
     *
     * @return The getters as one indented string.
     */
    final String getters() {
        //TODO generate the setter calls.
        return "";
    }

    /**
     * Generate the array of fields in top down declaration order. Top down
     * means the super stuff first.
     *
     * @return the array of all declared fields in the class hierarchy.
     */
    final Field[] getAllFieldsInClassHierarchy() {
        List<Field[]> fieldArrays = new ArrayList<>();
        //TODO
        return new Field[0];
    }

    /**
     * Produce a getter call like getName().
     *
     * @param f for field
     *
     * @return getName() for field name.
     */
    String getterName( Field f ) {
        //TODO
        return "get()";
    }

    /**
     * Try to find the Annotation @ID and if that fails the field called "id".
     *
     * New strategy. annotation, field named id, field named entitynameid,
     *
     * @return the field.
     *
     * @throws NoSuchFieldError after two attempts
     */
    Field getKeyField() {
//        Field firstField = entityType.getDeclaredFields()[ 0 ];
        return Stream.of( entityType.getDeclaredFields() )
//                .peek( f -> { System.out.println(f);} )
                .filter( f -> f.getAnnotation( ID.class ) != null )
                .findFirst()
                .or( this::getFieldNamedId )
                .or( this::getFieldNamedEntityId )
                //                .orElse( null );
                .orElseThrow( () -> new NoSuchFieldError(
                "Can't infer id field for class " + entityType.getName() ) );
    }

    Optional<Field> getFieldNamedId() {
        return Stream.of( entityType.getDeclaredFields() ).filter(
                f -> "id".equalsIgnoreCase( f.getName() ) )
                .findFirst();
    }

    Optional<Field> getFieldNamedEntityId() {
        return Stream.of( entityType.getDeclaredFields() ).filter(
                f -> ( entityType.getSimpleName().toLowerCase() + "id" )
                        .equalsIgnoreCase( f.getName() ) )
                .findFirst();
    }
}
