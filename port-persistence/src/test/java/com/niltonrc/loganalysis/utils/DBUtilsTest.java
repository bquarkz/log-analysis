package com.niltonrc.loganalysis.utils;

import com.niltonrc.loganalysis.entities.event.EventEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class DBUtilsTest
{
    @Test
    public void testPrepareQuestionMarks()
    {
        final DBUtils dbUtils = new DBUtils();
        final String s = dbUtils.prepareQuestionMarks( 5 );
        Assert.assertNotNull( s );
        Assert.assertEquals( "?,?,?,?,?", s );
    }

    @Test
    public void testDoBatch() throws SQLException
    {
        final DBUtils dbUtils = new DBUtils();
        final DataSource dataSource = Mockito.mock( DataSource.class );
        final Connection connection = Mockito.mock( Connection.class );
        final PreparedStatement statement = Mockito.mock( PreparedStatement.class );
        when( dataSource.getConnection() ).thenReturn( connection );
        when( connection.getAutoCommit() ).thenReturn( false );
        when( connection.prepareStatement( anyString() ) ).thenReturn( statement );

        long n = dbUtils.doBatch( null, dataSource,
                "sql", 10L, Stream.of( new EventEntity(),
                        new EventEntity(),
                        new EventEntity(),
                        new EventEntity(),
                        new EventEntity() ).collect( Collectors.toList() ),
                ( e ) -> {}, ( ps, e ) -> {} );

        Assert.assertEquals( 5, n );
        Mockito.verify( statement, times( 5 ) ).addBatch();
        Mockito.verify( statement, times( 1 ) ).executeBatch();
        Mockito.verify( statement, times( 1 ) ).clearBatch();
        Mockito.verify( connection, times( 1 ) ).commit();
    }


}
