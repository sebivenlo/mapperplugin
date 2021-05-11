package genericdao.dao;

import genericmapper.FieldPair;
import genericmapper.Mapper;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.StreamSupport;
import nl.fontys.sebivenlo.sebiannotations.Generated;
import nl.fontys.sebivenlo.sebiannotations.ID;
import nl.fontys.sebivenlo.sebiannotations.TableName;

/**
 * Data Access Object with transactions.
 *
 * A DAO has a few simple operations to support the traditional persistent
 * storage work:
 * <dl>
 * <dt>Create</dt> <dd>called {@code save(Entity e)} here.</dd>
 * <dt>Read</dt> <dd>called {@code Optional<Entity> get(Key k} here.</dd>
 * <dt>Update</dt> <dd>called {@code Entity update(Entity e)} here.</dd>
 * <dt>Delete</dt> <dd>called {@code void delete(Entity e)} here.</dd>
 * <dt>Get all</dt><dd>called {@code Collection<Entity> getAll()} here.</dd>
 * </dl>
 *
 * This DAO can participate in transactions by passing around a transaction
 * token. The implementing class of this interface is advised to have a
 * constructor accepting the token.
 *
 * The implementations that need to forward checked exceptions should wrap these
 * exceptions in a appropriate unchecked variant, or just log them.
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 * @param <E> the type of Entity
 * @param <K> Key to the entity
 */
public interface DAO<E extends Serializable, K extends Serializable> extends
        AutoCloseable {

    /**
     * Get the entity by id, if any.
     *
     * @param id of the entity
     *
     * @return a nullable optional containing the entity or not.
     */
    Optional<E> get( K id );

    /**
     * Get all entities.
     *
     * @return all entities available through this dao.
     */
    List<E> getAll();

    /**
     * Get by column keyvalues pairs. Convenience method to get a entities by
     * field=value[,field=value].
     *
     * @param keyValues even sized parameter list like
     * ("departmentid",1,"firstname", "Piet").
     *
     * @return the collection of enities matching key value pairs.
     */
    default List<E> getByColumnValues( Object... keyValues ) {
        throw new UnsupportedOperationException( "Not yet available" );
    }

    /**
     * Persist an entity.
     *
     * @param e to save
     *
     * @return the update entity, primary key and all
     */
    Optional<E> save( E e );

    /**
     * Update and entity. In a typical database scenario, all fields are set to
     * the new values. It is up to the user to ensure that the id value is
     * stable throughout this operation.
     *
     * @param e entity to update
     *
     * @return the updated entity
     */
    E update( E e );

    /**
     * Delete t using its key (id). This method extracts the key and uses that
     * to delete the entity.
     *
     * @param e entity to delete
     */
    void deleteEntity( E e );

    /**
     * Delete an entity by key.
     *
     * @param k the key (id) of the entity to delete
     */
    void deleteById( K k );

    /**
     * Start a transaction and create a token carrying the transaction relevant
     * information.
     *
     * @return the token
     *
     * @throws java.lang.Exception on error, eg database connection failure
     */
    default TransactionToken startTransaction() throws Exception {
        return null;
    }

    /**
     * Pass an existing transaction token to this DAO. In the default
     * implementation, the token is silently ignored.
     *
     * @param tok to use for the remainder.
     *
     * @return this DAO
     */
    default DAO<E, K> setTransactionToken( TransactionToken tok ) {
        return this;
    }

    /**
     * Get the transaction token of this DAO, if any.
     *
     * @return the token or null if no transaction has been started.
     */
    default TransactionToken getTransactionToken() {
        return null;
    }

    /**
     * Get the number of entities accessible through this DAO.
     *
     * @return the size of the backing store (table, lst, map...)
     */
    default int size() {
        return 0;
    }

    /**
     * Get the highest id used in this DAO.
     *
     * @return the last used number.
     */
    default int lastId() {
        return 0;
    }

    /**
     * Default no-op close.
     *
     * @throws Exception whenever the implementer sees it fit.
     */
    @Override
    default void close() throws Exception {
    }

    /**
     * Save all entities, returning the result in a collection with the saved
     * versions of the entities, generated fields and all.
     *
     * The caller is advised to give this DAO a transaction token, so this
     * operation can perform atomically for those persistence implementations
     * that support proper commit and rollback.
     *
     * Implementations that recreate resources on every call of
     * {@code dao.save(e)} should probably overwrite this default implementation
     * for improved performance and memory efficiency.
     *
     * @param entities to save
     *
     * @return the saved entities
     *
     * @since 0.4
     */
    default List<? extends E> saveAll( List<E> entities ) {
        return StreamSupport.stream( entities.spliterator(), false )
                .map( e -> this.save( e ).get() ).collect( toList() );
    }

    /**
     * Save the given entities, returning the result as a list.
     *
     * This default implementation will benefit from any improvemenst by an
     * overwritten {@code saveAll(Collection &lt;E&gt;entities)}
     *
     * @param entities to save
     *
     * @return the saved versions of the entities in a list.
     *
     * @since 0.4
     */
    @SuppressWarnings( "unchecked" )
    default List<? extends E> saveAll( E... entities ) {
        return saveAll( Arrays.asList( entities ) );
    }

    /**
     * Delete the given entities.
     *
     * @param entities to delete.
     *
     * @since 0.5
     */
    default void deleteAll( Iterable<E> entities ) {
        StreamSupport.stream( entities.spliterator(), false ).forEach(
                this::deleteEntity );
    }

    /**
     * Delete the given entities, array version.
     *
     * @param entities to delete
     *
     * @since 0.5
     */
    @SuppressWarnings( "unchecked" )
    default void deleteAll( E... entities ) {
        deleteAll( List.of( entities ) );
    }

    /**
     * Execute a query with some query text for a prepared statement. Usecase:
     * in a RDBMS (postgres) somtimes you want to call a function in a query,
     * which is not directly supported by the DAO. This implementation returns
     * an empty list. Usefull work is left to the implementing class.
     *
     * @param queryText sql with positional parameters
     * @param params the values for the position parameters
     *
     * @return a list of E.
     */
    default List<E> anyQuery( String queryText, Object... params ) {
        return List.of();
    }

    /**
     * Get the mapper for this entity.
     *
     * @return the mapper.
     */
    Mapper<E, K> getMapper();

    /**
     * Extract the key field from an entity.
     *
     * @param e entity
     *
     * @return the key.
     */
    default K extractId( E e ) {
        return getMapper().keyExtractor().apply( e );
    }

    /**
     * Return the list of field names of enitity that are considered for
     * persistence. This implementation does not consider field modifiers such
     * as transient.
     *
     * @return the list of field names.
     */
    default List<String> persistentFieldNames() {
        return getMapper().entityFields()
                .stream().map( Field::getName ).collect( toList() );
    }

    /**
     * Get the names of the generated fields.
     *
     * @return the list of names.
     */
    default public List<String> generatedFields() {
        //TODO cache in factory map.
        Field[] declaredFields = getMapper().getEntityType().getDeclaredFields();

        return Arrays.stream( declaredFields )
                .filter( f -> isGenerated( f ) )
                .map( Field::getName )
                .collect( toList() );
    }

    /**
     * Check if field has annotation Generated or annotation ID with generated
     * is false.
     *
     * @param f field.
     *
     * @return boolean if field is supposed to be generated when stored in the
     * backing store.
     */
    static boolean isGenerated( Field f ) {
        ID idAnnotation = f.getAnnotation( ID.class );
        Generated genAnnotation = f.getAnnotation( Generated.class );
        System.out.println( "field " + f
                + " id Annotaion =" + idAnnotation
                + " genAnnotation = " + genAnnotation );
        return null != genAnnotation || ( null != idAnnotation && idAnnotation
                .generated() );
    }

    /**
     * Get the table name for the entity.
     *
     * @return the name of the table in the database
     */
    default String tableName() {
        return tableName( getMapper().getEntityType() );
    }

    /**
     * Static helper to get uniform generation of the table name, either from
     * the {@code @TableName} annotation or from using 'simple plural', by appending a s
     * to the lower case version of the entity name.
     * @param entityType to reflect 
     * @return the table name
     */
    static String tableName( Class<?> entityType ) {
        TableName annotation = entityType.getAnnotation(
                TableName.class );
        if ( annotation != null ) {
            return annotation.value();
        }
        return entityType.getSimpleName().toLowerCase() + 's';
    }

    /**
     * Remove the generated fields from the stream, so they can be sued to
     * populate declare and populate a prepared statement.
     *
     * @param entity to process
     *
     * @return the list of field pairs without the fields that are generated,
     * either by being tagged with ID or Generated.
     */
    default List<FieldPair> dropGeneratedFields( E entity ) {
        Mapper<E, K> mapper = getMapper();
        Predicate<FieldPair> fpl = ( FieldPair fp )
                -> !mapper.generatedFieldNames().contains( fp.key() );
        return mapper.stream( entity ).filter( fpl ).collect( toList() );
    }

    /**
     * Remove all entities from the backing store of this dao. Optional
     * operation.
     */
    default void dropAll() {
    }
}
