package club.sqlhub.config;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway 11.x only supports PostgreSQL up to 16.x in its version range check,
 * causing "Unsupported Database: PostgreSQL 17.x" when connecting to Supabase.
 * This customizer wraps the DataSource so Flyway sees "16.0" during version
 * detection only — all real DB operations still run against the actual server.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayConfigurationCustomizer flywayVersionPatchCustomizer(DataSource dataSource) {
        return config -> config.dataSource(patchedDataSource(dataSource));
    }

    private static DataSource patchedDataSource(DataSource original) {
        return (DataSource) Proxy.newProxyInstance(
            DataSource.class.getClassLoader(),
            new Class[]{DataSource.class},
            (proxy, method, args) -> {
                if ("getConnection".equals(method.getName())) {
                    Connection conn = (Connection) method.invoke(original, args);
                    return patchedConnection(conn);
                }
                return method.invoke(original, args);
            }
        );
    }

    private static Connection patchedConnection(Connection original) {
        return (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class[]{Connection.class},
            (proxy, method, args) -> {
                if ("getMetaData".equals(method.getName())) {
                    return patchedMetaData(original.getMetaData());
                }
                return method.invoke(original, args);
            }
        );
    }

    private static DatabaseMetaData patchedMetaData(DatabaseMetaData original) {
        return (DatabaseMetaData) Proxy.newProxyInstance(
            DatabaseMetaData.class.getClassLoader(),
            new Class[]{DatabaseMetaData.class},
            (proxy, method, args) -> {
                if ("getDatabaseProductVersion".equals(method.getName())) {
                    return "16.0";
                }
                if ("getDatabaseMajorVersion".equals(method.getName())) {
                    return 16;
                }
                if ("getDatabaseMinorVersion".equals(method.getName())) {
                    return 0;
                }
                return method.invoke(original, args);
            }
        );
    }
}
