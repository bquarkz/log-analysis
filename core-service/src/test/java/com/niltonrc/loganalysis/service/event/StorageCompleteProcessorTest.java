package com.niltonrc.loganalysis.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niltonrc.loganalysis.contract.IEventRepository;
import com.niltonrc.loganalysis.event.Event;
import com.niltonrc.loganalysis.utils.EventQueue;
import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class StorageCompleteProcessorTest
{
    private final EnhancedRandom pojoFactory;

    {
        this.pojoFactory = EnhancedRandomBuilder
                .aNewEnhancedRandomBuilder()
                .seed( System.currentTimeMillis() )
                .build();
    }

    private EnhancedRandom getPojoFactory()
    {
        return pojoFactory;
    }

    private List< Event > buildEventList( int size )
    {
        return getPojoFactory().objects( Event.class, size ).collect( Collectors.toList() );
    }

    @Test
    public void testStorageProcessor() throws Exception
    {
        final EventQueue< List< Event > > eventQueue = new EventQueue<>();
        eventQueue.top( buildEventList( 3 ) );
        eventQueue.top( buildEventList( 8 ) );
        eventQueue.top( buildEventList( 5 ) );
        eventQueue.top( buildEventList( 12 ) );
        final EventConverter eventConverter = new EventConverter( new ObjectMapper() );
        final IEventRepository eventRepository = Mockito.mock( IEventRepository.class );
        when( eventRepository.batchInsert( anyList() ) ).then( a -> (long)( (List)a.getArguments()[ 0 ] ).size() );

        final StorageCompleteProcessor processor = new StorageCompleteProcessor( 3, eventConverter, eventRepository, eventQueue );
        final Long number = processor.call();
        Assert.assertNotNull( number );
        Assert.assertEquals( 28L, (long)number );
        verify( eventRepository, times( 10 ) ).batchInsert( anyList() );
    }
}
