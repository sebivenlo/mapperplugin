package genericdao.pgdao;

import genericdao.dao.DAOException;
import genericmapper.FieldPair;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Extracted interface to make the QueryExecutor mockable, so we can test the
 * dao without a database.
 *
 * @author Pieter van den Hombergh {@code Pieter.van.den.Hombergh@gmail.com}
 * @param <E> entity type
 * @param <K> key in entity
 */
public abstract class AbstractQueryExecutor<E extends Serializable, K extends Serializable> {

    public abstract Optional<E> doGet( final Connection con, String sql, K id )
            throws DAOException;

    abstract void doDeleteAll( Connection con, String sql, List<K> keys ) throws
            DAOException;

    abstract void doDelete( final Connection con, String sql, K k ) throws
            DAOException;

    abstract E recordToEntity( final ResultSet rs ) throws SQLException;

    abstract E doUpdate( final Connection c, String sql, E e, K key ) throws
            DAOException;

    abstract List<E> doGetAll( final Connection c, String sql ) throws
            DAOException;

    abstract List<E> doGetByColumnValues( Connection c, String sql,
            List<FieldPair> fp ) throws DAOException;

    abstract int doExecuteIntQuery( final Connection c, String sql, K k ) throws
            DAOException;

    abstract int doExecuteIntQuery( final Connection c, String sql );

    /**
     * Execute a query that produces a List of E.
     *
     * @param con connection to use
     * @param queryText sql text
     * @param params positional parameters in the query
     * @return list of e, produced by the query.
     */
    abstract List<E> doAnyQuery( Connection con, String queryText,
            Object... params );

    abstract Optional<E> doSave( final Connection c, String queryText, E t );

    abstract PreparedStatement populateSaveStatement( Connection c,
            String queryText, E e ) throws SQLException;

    abstract List<E> doSaveAll( final Connection c, String sql, List<E> entities,
            List<E> result ) throws DAOException;

    abstract PreparedStatement fillPreparedStatement(
            final PreparedStatement pst, List<FieldPair> fields ) throws
            SQLException;

    abstract void doAnyVoidQuery( Connection con, String queryText,
            Object... params );
}
