package genericdao.pgdao;

import genericdao.dao.DAO;
import genericmapper.Mapper;
import static java.lang.String.format;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import nl.fontys.sebivenlo.sebiannotations.TableName;

/**
 * Class to compute and cache query strings.
 *
 * The queries are to be computed from the meta data on the java entity type in
 * the mapper. After the first invocation of on of the non private method, the
 * same should be returned on each call.
 *
 *
 * @author Pieter van den Hombergh {@code Pieter.van.den.Hombergh@gmail.com}
 */
public class QueryFactory {

    final Mapper<?, ?> mapper;

    ConcurrentMap<String, String> queryTextCache = new ConcurrentHashMap<>();

    public QueryFactory( Mapper<?, ?> mapper ) {
        this.mapper = mapper;
    }

    /**
     * Returned a value from the cache.
     *
     * @return
     */
    String allColumns() {
        return this.queryTextCache
                .computeIfAbsent( "allColumns", x -> computeAllColumns() );
    }

    /**
     * Compute the value of all columns joins with a comma.
     *
     * @return text
     */
    private String computeAllColumns() {
        //TODO
        return "";
    }

    /**
     * The name of the key column.
     *
     * @return the name of the key column
     */
    String idName() {
        return this.queryTextCache
                .computeIfAbsent( "idName", x -> mapper.getKeyFieldName() );
    }

    String getQueryText() {
        return queryTextCache
                .computeIfAbsent( "selectsingle", ( x )
                        -> computeGetQueryText() );
    }

    /**
     * Compute the select query.
     *
     * @return the text
     */
    private String computeGetQueryText() {
        //TODO
        return "";
    }

    /**
     * Get the table name fom the @TableName annotation.
     *
     * @return the table name or a synthesized name using simple plural.
     */
    String tableName() {
        return DAO.tableName( mapper.getEntityType() );
    }

    /**
     * Get the delete query form the cache.
     *
     * @return the text
     */
    String deleteQueryText() {
        return queryTextCache
                .computeIfAbsent( "delete", x -> computeDeleteQueryText() );
    }

    private String computeDeleteQueryText() {
        //TODO
        return "";
    }

    String updateQueryText() {
        return queryTextCache
                .computeIfAbsent( "update", x -> computeUpdateQueryText()
                );
    }

    private String computeUpdateQueryText() {
        //TODO
        return "";
    }

    final String saveQueryText() {
        return queryTextCache
                .computeIfAbsent( "save", x -> computeSaveQueryText() );
    }

    final String makePlaceHolders( final int count ) {
        String[] qm = new String[ count ];
        Arrays.fill( qm, "?" );
        return String.join( ",", qm );
    }

    private String computeSaveQueryText() {
        //TODO
        return "";
    }

    String allQuery() {
        return queryTextCache
                .computeIfAbsent( "all", x -> computeAllQueryText() );
    }

    private String computeAllQueryText() {
        //TODO
        return "";
    }

    String lastIdQuery() {
        return queryTextCache
                .computeIfAbsent( "lastIdQuery", x -> computeLastIdQuery() );
    }

    private String computeLastIdQuery() {
        //TODO
        return "";
    }

    String dropallQuery() {
        return queryTextCache
                .computeIfAbsent( "dropAllQuery", x -> computeDropallQuery() );

    }

    String computeDropallQuery() {
        return "truncate " + tableName() + " restart identity cascade";
    }
}
