package com.niltonrc.loganalysis.configuration;

import org.hsqldb.jdbc.JDBCDataSource;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.File;
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
    DataSource getDataSource( final DatabaseResource dbResource )
    {

        final JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl( dbResource.getUrl() );
        dataSource.setUser( dbResource.getUsername() );
        dataSource.setPassword( dbResource.getPassword() );
        return dataSource;
    }

    @Bean
    FlywayMigrationStrategy getFlyWayMigrationStrategy( final DatabaseResource dbResource )
    {
        return flyway -> {
            try( final Connection c = DriverManager
                    .getConnection( dbResource.getUrl(), dbResource.getUsername(), dbResource.getPassword() ) )
            {
                if( !dbResource.isInMemory() && !new File( dbResource.getLocal() ).exists() )
                {
                    LoggerFactory.getLogger( DataSourceConfiguration.class ).info( "created new database using: " + dbResource );
                }
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
        private final String local;
        private final boolean inMemory;

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
            this.local = url.substring( url.indexOf( "jdbc:hsqldb:file:" ) );
            this.inMemory = url.startsWith( "jdbc:hsqldb:mem:" );
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
                    "jdbc:hsqldb:file:" + local,
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

        public String getLocal()
        {
            return local;
        }

        public boolean isInMemory()
        {
            return inMemory;
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
