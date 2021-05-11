package genericdao.dao;

import genericmapper.Mapper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Super of all DAO factories. Registers mappers that help to understand 
 * the entities that are to be mapped by this DAO.
 * 
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
public abstract class DAOFactory {

    /**
     * Map type to TypeMappper.
     */
    private final ConcurrentMap<Class<? extends Serializable>, 
            Mapper<? extends Serializable, ? extends Serializable>> mappers
            = new ConcurrentHashMap<>();

    /**
     * Register a type with its mapper.
     *
     * @param forClass the class to be mapped
     * @param mappedBy this mapper
     */
    public void registerMapper( Class<? extends Serializable> forClass,
            Mapper<? extends Serializable, ? extends Serializable> mappedBy ) {
        mappers.put( forClass, mappedBy );
    }

    /**
     * Create a DAO for a given entity class indexed by a key class.
     *
     * @param <K> the key generic type
     * @param <E> the entity generic type
     * @param forClass actual type of the entity
     * @return the prepared DAO.
     */
    public abstract <E extends Serializable, K extends Serializable> DAO<E,K> 
        createDao( Class<E> forClass );

    /**
     *
     * Create a DAO for a given entity class indexed by a key class, prepared to
     * participate in a Transaction.
     *
     * @param <K> the key generic type
     * @param <E> the entity generic type
     * @param forClass actual type of the entity
     * @param token transaction token.
     * @return the prepared DAO
     */
    public abstract <E extends Serializable, K extends Serializable> DAO<E, K>
         createDao( Class<E> forClass,
            TransactionToken token );
}
