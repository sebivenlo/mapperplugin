package genericdao.pgdao;

import genericdao.dao.DAOException;
import genericdao.dao.TransactionToken;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Token carrying transaction information, such as the active connection.
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
public class PGTransactionToken implements TransactionToken {

    private final Connection connection;

    /**
     * Create a token using a connection.
     *
     * @param connection to hold
     * @throws DAOException when the connection can't be used.
     */
    public PGTransactionToken( Connection connection ) {
        this.connection = connection;
        try {
            this.connection.setAutoCommit( false );
        } catch ( SQLException ex ) {
            Logger.getLogger( PGTransactionToken.class.getName() ).
                    log( Level.SEVERE, null, ex );
            throw new DAOException( ex.getMessage(), ex );

        }
    }

    @Override
    public void rollback() throws Exception {
        this.connection.rollback();
    }

    @Override
    public void commit() throws Exception {
        this.connection.commit();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        if ( !this.connection.isClosed() ) {
            this.connection.close();
        }
    }

}
