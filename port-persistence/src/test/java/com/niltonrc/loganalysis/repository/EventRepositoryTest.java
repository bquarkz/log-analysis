package com.niltonrc.loganalysis.repository;

import com.niltonrc.loganalysis.entities.event.EventEntity;
import com.niltonrc.loganalysis.utils.DBUtils;
import com.niltonrc.loganalysis.utils.LogPrinter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;

public class EventRepositoryTest
{
    private List< EventEntity > getEventEntityList()
    {
        return Stream.of(
                new EventEntity(),
                new EventEntity(),
                new EventEntity() ).collect( Collectors.toList() );
    }

    @Test
    public void testSimpleRepository()
    {
        final DataSource dataSource = Mockito.mock( DataSource.class );
        final DBUtils dbUtils = Mockito.mock( DBUtils.class );
        Mockito.when( dbUtils.doBatch( any( Logger.class ), any( DataSource.class ), anyString(), anyLong(), anyList(), any(), any() ) )
               .then( a -> (long)( (List)a.getArguments()[ 4 ] ).size() );
        final EventRepository repository = new EventRepository( new LogPrinter(), dataSource, dbUtils );
        long n = repository.batchInsert( getEventEntityList() );
        Assert.assertEquals( 3, n );
        Mockito.verify( dbUtils, Mockito.times( 1 ) )
               .prepareQuestionMarks( anyInt() );
        Mockito.verify( dbUtils, Mockito.times( 1 ) )
               .doBatch( any( Logger.class ), any( DataSource.class ), anyString(), anyLong(), anyList(), any(), any() );
    }
}
