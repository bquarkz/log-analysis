package com.niltonrc.loganalysis.service.event;

import com.niltonrc.loganalysis.event.Entry;
import com.niltonrc.loganalysis.event.EntryState;
import com.niltonrc.loganalysis.event.Event;
import com.niltonrc.loganalysis.event.EventManager;
import com.niltonrc.loganalysis.utils.EventQueue;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeRemainderProcessorTest
{
    @Test
    public void test() throws Exception
    {
        EventQueue< List< Event > > complete = new EventQueue<>();
        EventQueue< Map< String, EventManager > > remainder = new EventQueue<>();

        {
            Map< String, EventManager > map = new HashMap<>();
            map.put( "id1", new EventManager( new Entry( "id1", EntryState.FINISHED, 1000L ) ) );
            map.put( "id2", new EventManager( new Entry( "id2" ) ) );
            map.put( "id4", new EventManager( new Entry( "id4" ) ) );
            map.put( "id5", new EventManager( new Entry( "id5" ) ) );
            remainder.top( map );
        }

        {
            Map< String, EventManager > map = new HashMap<>();
            map.put( "id1", new EventManager( new Entry( "id1", EntryState.STARTED, 1010L ) ) );
            remainder.top( map );
        }

        MergeRemainderProcessor proc = new MergeRemainderProcessor( complete, remainder );
        final long reRemainder = proc.call();
        System.out.println( complete.size() );
        System.out.println( remainder.size() );
        System.out.println( reRemainder );
    }
}
