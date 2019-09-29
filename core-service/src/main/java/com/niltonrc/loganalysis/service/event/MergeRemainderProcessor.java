package com.niltonrc.loganalysis.service.event;

import com.niltonrc.loganalysis.event.Entry;
import com.niltonrc.loganalysis.event.Event;
import com.niltonrc.loganalysis.event.EventManager;
import com.niltonrc.loganalysis.utils.EventQueue;
import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class MergeRemainderProcessor implements Callable< Long >
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
    private final EventQueue< List< Event > > completeQueue;
    private final EventQueue< Map< String, EventManager > > remainderQueue;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public MergeRemainderProcessor(
            EventQueue< List< Event > > completeQueue,
            EventQueue< Map< String, EventManager > > remainderQueue )
    {
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
        Optional< Map< String, EventManager > > tail1;
        Optional< Map< String, EventManager > > tail2;
        while( ( tail1 = remainderQueue.tail() ).isPresent() )
        {
            final Map< String, EventManager > reRemainder = new HashMap<>();
            final List< EventManager > complete = new ArrayList<>();
            if( ( tail2 = remainderQueue.tail() ).isPresent() )
            {
                Map< String, EventManager > r1 = tail1.get();
                Map< String, EventManager > r2 = tail2.get();

                for( String k1 : r1.keySet() )
                {
                    final EventManager em1 = r1.get( k1 );
                    final EventManager em2 = r2.get( k1 );
                    if( em2 != null )
                    {
                        em1.merge( em2 );
                        complete.add( em1 );
                        r2.remove( k1 );
                    }
                    else
                    {
                        reRemainder.put( k1, em1 );
                    }
                }

                for( String k2 : r2.keySet() )
                {
                    final EventManager em2 = r2.get( k2 );
                    reRemainder.put( k2, em2 );
                }

                remainderQueue.top( reRemainder );
                final List< Event > eventComplete = complete
                        .stream()
                        .map( EventManager::getEvent )
                        .collect( Collectors.toList() );
                completeQueue.top( eventComplete );
            }
            else
            {
                return (long)tail1.get().size();
            }
        }
        return 0L;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
