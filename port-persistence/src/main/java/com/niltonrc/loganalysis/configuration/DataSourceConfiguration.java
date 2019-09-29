package com.niltonrc.loganalysis.configuration;

import com.niltonrc.loganalysis.utils.LogPrinter;
import org.hsqldb.jdbc.JDBCPool;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSourceConfiguration
{
    @Bean
    DatabaseResource getDatabaseResource( final Environment environment )
    {
        final String databaseOutput = environment.getRequiredProperty( "database.output" );
        return DatabaseResource.local( databaseOutput );
    }

    @Bean
    DataSource getDataSource( final LogPrinter logPrinter, final DatabaseResource dbResource )
    {
        logPrinter.print( LoggerFactory.getLogger( DataSourceConfiguration.class ), "Building database..." );
        final JDBCPool pool = new JDBCPool();
        pool.setUrl( dbResource.getUrl() );
        pool.setUser( dbResource.getUsername() );
        pool.setPassword( dbResource.getPassword() );
        return pool;
    }

    @Bean
    FlywayMigrationStrategy getFlyWayMigrationStrategy( final DatabaseResource dbResource )
    {
        return flyway -> {
            try( final Connection c = DriverManager
                    .getConnection( dbResource.getUrl(), dbResource.getUsername(), dbResource.getPassword() ) )
            {
                LoggerFactory.getLogger( DataSourceConfiguration.class ).info( "database: " + dbResource );
            }
            catch( SQLException e )
            {
                throw new RuntimeException( e );
            }

            flyway.configure()
                  .locations( "classpath:db/migration/" )
                  .dataSource( dbResource.getUrl(), dbResource.getUsername(), dbResource.getPassword() )
                  .load()
                  .migrate();
        };
    }

    private static class DatabaseResource
    {
        private final String driver;
        private final String url;
        private final String username;
        private final String password;

        private DatabaseResource(
                String driver,
                String url,
                String username,
                String password )
        {
            this.driver = driver;
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public static DatabaseResource inMemory()
        {
            return new DatabaseResource( "org.hsqldb.jdbc.JDBCDriver",
                    "jdbc:hsqldb:mem:log-analysis",
                    "sa",
                    "" );
        }

        public static DatabaseResource local( String local )
        {
            return new DatabaseResource( "org.hsqldb.jdbc.JDBCDriver",
                    "jdbc:hsqldb:file:" + local + ";create=true;shutdown=true;",
                    "sa",
                    "" );
        }

        public String getDriver()
        {
            return driver;
        }

        public String getUrl()
        {
            return url;
        }

        public String getUsername()
        {
            return username;
        }

        public String getPassword()
        {
            return password;
        }

        @Override
        public String toString()
        {
            return "DatabaseResource{" +
                    "driver='" + driver + '\'' +
                    ", url='" + url + '\'' +
                    ", username='" + username + '\'' +
                    ", password='***'" +
                    '}';
        }
    }
}
