package genericdao.memory;

import genericdao.dao.DAO;
import genericdao.dao.DAOFactory;
import genericdao.dao.TransactionToken;
import java.io.Serializable;

/**
 *
 * @author "Pieter van den Hombergh {@code p.vandenhombergh@fontys.nl}"
 */
public class InMemoryDAOFactory extends DAOFactory {

    @Override
    public <E extends Serializable, K extends Serializable> DAO<E, K> createDao( Class<E> forClass ) {
        return new InMemoryDAO<>( forClass );
    }

    /**
     * The created daoa do not use the transaction tkones.
     *
     * @param <E>      generic entity
     * @param <K>      generic key
     * @param forClass entity type
     * @param token    to pass
     * @return the dao.
     */
    @Override
    public <E extends Serializable, K extends Serializable> DAO<E, K> createDao( Class<E> forClass, TransactionToken token ) {
        return new InMemoryDAO<E, K>( forClass ).setTransactionToken( token );
    }

}
