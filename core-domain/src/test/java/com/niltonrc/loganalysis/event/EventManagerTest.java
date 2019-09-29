package com.niltonrc.loganalysis.event;

import org.junit.Assert;
import org.junit.Test;

public class EventManagerTest
{
    @Test
    public void testASimpleFlow()
    {
        final Entry first = new Entry();
        first.setId( "id" );
        first.setState( EntryState.STARTED );
        first.setTimestamp( 10L );
        final Entry second = new Entry();
        second.setId( "id" );
        second.setState( EntryState.FINISHED );
        second.setTimestamp( 15L );

        final EventManager eventManager = new EventManager( first );
        Assert.assertTrue( eventManager.merge( second ) );
        Assert.assertEquals( "id", eventManager.getEvent().getId() );
        Assert.assertEquals( 5L, eventManager.getEvent().getDuration() );
    }

    @Test
    public void testAnInverseFlow()
    {
        final Entry first = new Entry();
        first.setId( "id" );
        first.setState( EntryState.FINISHED );
        first.setTimestamp( 15L );
        final Entry second = new Entry();
        second.setId( "id" );
        second.setState( EntryState.STARTED );
        second.setTimestamp( 10L );

        final EventManager eventManager = new EventManager( first );
        Assert.assertTrue( eventManager.merge( second ) );
        Assert.assertEquals( "id", eventManager.getEvent().getId() );
        Assert.assertEquals( 5L, eventManager.getEvent().getDuration() );
    }
}
