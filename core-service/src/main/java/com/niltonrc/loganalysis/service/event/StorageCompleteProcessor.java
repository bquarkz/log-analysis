package com.niltonrc.loganalysis.service.event;

import com.niltonrc.loganalysis.constants.DBConstants;
import com.niltonrc.loganalysis.contract.IEventRepository;
import com.niltonrc.loganalysis.entities.event.EventEntity;
import com.niltonrc.loganalysis.event.Event;
import com.niltonrc.loganalysis.utils.EventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class StorageCompleteProcessor implements Callable< Long >
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final int dbBatchSize;
    private final EventConverter eventConverter;
    private final IEventRepository eventRepository;
    private final EventQueue< List< Event > > completeQueue;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public StorageCompleteProcessor(
            int dbBatchSize,
            EventConverter eventConverter,
            IEventRepository eventRepository,
            EventQueue< List< Event > > completeQueue )
    {
        this.dbBatchSize = dbBatchSize;
        this.eventConverter = eventConverter;
        this.eventRepository = eventRepository;
        this.completeQueue = completeQueue;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Factories
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters And Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Long call() throws Exception
    {
        long total = 0L;
        Optional< List< Event > > tail;
        List< Event > batch = new ArrayList<>( dbBatchSize + 1 );
        while( ( tail = completeQueue.tail() ).isPresent() )
        {
            final List< Event > events = tail.get();
            int toIndex = getToIndex( batch, events );
            final List< Event > toAdd = events.subList( 0, toIndex );
            batch.addAll( toAdd );

            if( toIndex < events.size() )
            {
                final List< Event > toQueue = events.subList( toIndex, events.size() );
                completeQueue.top( toQueue );
            }

            if( batch.size() >= dbBatchSize )
            {
                final List< EventEntity > eventEntities = eventConverter.mapToEventEntities( batch );
                total += eventRepository.batchInsert( eventEntities );
                batch = new ArrayList<>( dbBatchSize + 1 );
            }
        }

        if( !batch.isEmpty() )
        {
            total += eventRepository.batchInsert( eventConverter.mapToEventEntities( batch ) );
        }

        return total;
    }

    private int getToIndex(
            List< Event > batch,
            List< Event > eventEntities )
    {
        int maximumToAdd = dbBatchSize - batch.size();
        int size = eventEntities.size();
        return Math.min( maximumToAdd, size );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
