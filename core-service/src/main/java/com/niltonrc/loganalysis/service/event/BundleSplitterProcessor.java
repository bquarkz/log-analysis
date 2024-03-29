package com.niltonrc.loganalysis.service.event;

import com.niltonrc.loganalysis.constants.GeneralConstants;
import com.niltonrc.loganalysis.event.Event;
import com.niltonrc.loganalysis.event.EventManager;
import com.niltonrc.loganalysis.utils.EventQueue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BundleSplitterProcessor implements Callable< Long >
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
    private final EventQueue< Future< EventBundle > > queue;
    private final EventQueue< List< Event > > completeQueue;
    private final EventQueue< Map< String, EventManager > > remainderQueue;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public BundleSplitterProcessor(
            EventQueue< Future< EventBundle > > queue,
            EventQueue< List< Event > > completeQueue,
            EventQueue< Map< String, EventManager > > remainderQueue )
    {
        this.queue = queue;
        this.completeQueue = completeQueue;
        this.remainderQueue = remainderQueue;
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
        long entryProblems = 0L;
        Optional< Future< EventBundle > > tail;
        while( ( tail = queue.tail() ).isPresent() )
        {
            final Future< EventBundle > future = tail.get();
            try
            {
                final EventBundle bundle = future.get( GeneralConstants.TIME_TO_WAIT_IN_MS, TimeUnit.MILLISECONDS );
                completeQueue.top( bundle.getComplete() );
                remainderQueue.top( bundle.getRemainder() );
                entryProblems += bundle.getNumberOfInvalidEntries();
            }
            catch( TimeoutException ex )
            {
            }
        }
        return entryProblems;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
