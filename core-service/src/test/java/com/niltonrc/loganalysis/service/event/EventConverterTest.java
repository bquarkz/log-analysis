package com.niltonrc.loganalysis.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niltonrc.loganalysis.event.Entry;
import com.niltonrc.loganalysis.event.EntryState;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventConverterTest
{
    @Test
    public void testASmallExampleShouldBeFine()
    {
        final List< String > jsons = Stream
                .of( "{\"id\":\"scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}",
                     "{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}",
                     "{\"id\":\"scsmbstgrc\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}" )
                .collect( Collectors.toList() );
        final EventConverter converter = new EventConverter( new ObjectMapper() );
        final List< Entry > entries = converter.mapToEntries( jsons );
        Assert.assertNotNull( entries );
        Assert.assertEquals( 3, entries.size() );

        {
            Entry entry = entries.get( 0 );
            Assert.assertEquals( "scsmbstgra", entry.getId() );
            Assert.assertEquals( EntryState.STARTED, entry.getState() );
            Assert.assertEquals( "APPLICATION_LOG", entry.getType() );
            Assert.assertEquals( "12345", entry.getHost() );
            Assert.assertEquals( 1491377495212L, entry.getTimestamp() );
        }

        {
            Entry entry = entries.get( 1 );
            Assert.assertEquals( "scsmbstgrb", entry.getId() );
            Assert.assertEquals( EntryState.STARTED, entry.getState() );
            Assert.assertNull( entry.getType() );
            Assert.assertNull( entry.getHost() );
            Assert.assertEquals( 1491377495213L, entry.getTimestamp() );
        }

        {
            Entry entry = entries.get( 2 );
            Assert.assertEquals( "scsmbstgrc", entry.getId() );
            Assert.assertEquals( EntryState.FINISHED, entry.getState() );
            Assert.assertNull( entry.getType() );
            Assert.assertNull( entry.getHost() );
            Assert.assertEquals( 1491377495218L, entry.getTimestamp() );
        }
    }

    @Test
    public void testWhenIdIsNullEntryShouldDiscarded()
    {
        final List< String > jsons = Stream
                .of( "{\"state\":\"STARTED\", \"timestamp\":1491377495212}",
                     "{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}" )
                .collect( Collectors.toList() );
        final EventConverter converter = new EventConverter( new ObjectMapper() );
        final List< Entry > entries = converter.mapToEntries( jsons );
        Assert.assertNotNull( entries );
        Assert.assertEquals( 1, entries.size() );

        {
            Entry entry = entries.get( 0 );
            Assert.assertEquals( "scsmbstgrb", entry.getId() );
            Assert.assertEquals( EntryState.STARTED, entry.getState() );
            Assert.assertNull( entry.getType() );
            Assert.assertNull( entry.getHost() );
            Assert.assertEquals( 1491377495213L, entry.getTimestamp() );
        }
    }

    @Test
    public void testWhenStateIsNotCompatibleEntryShouldDiscarded()
    {
        final List< String > jsons = Stream
                .of( "{\"id\":\"scsmbstgra\", \"state\":\"INCOMPATIBLE\", \"timestamp\":1491377495212}",
                     "{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}" )
                .collect( Collectors.toList() );
        final EventConverter converter = new EventConverter( new ObjectMapper() );
        final List< Entry > entries = converter.mapToEntries( jsons );
        Assert.assertNotNull( entries );
        Assert.assertEquals( 1, entries.size() );

        {
            Entry entry = entries.get( 0 );
            Assert.assertEquals( "scsmbstgrb", entry.getId() );
            Assert.assertEquals( EntryState.STARTED, entry.getState() );
            Assert.assertNull( entry.getType() );
            Assert.assertNull( entry.getHost() );
            Assert.assertEquals( 1491377495213L, entry.getTimestamp() );
        }

    }

}
