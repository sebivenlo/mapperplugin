/**
 *
 * Simple yet powerful generic dao.
 * 
 *
 * <img src='doc-files/summary.dot.svg'  alt='package dependencies' >
 * 
 */
module nl.fontys.sebivenlo.genericdao {
    requires transitive nl.fontys.sebivenlo.genericmapper;
    requires java.logging;
    requires java.sql;
    requires java.naming;
    requires org.postgresql.jdbc;
    requires  nl.fontys.sebivenlo.sebiannotations;
    exports genericdao.dao;
    exports genericdao.memory;
    exports genericdao.pgdao;
}
