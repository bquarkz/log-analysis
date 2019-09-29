package com.niltonrc.loganalysis.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventProcessorTest
{
    @Test
    public void testEventProcessor() throws Exception
    {
        final EventConverter eventConverter = new EventConverter( new ObjectMapper() );
        final EventProcessor processor = new EventProcessor( eventConverter, Stream
                .of(
                    "{\"state\":\"FINISHED\", \"timestamp\":1491377495218}",
                    "{\"id\":\"scsmbstgrx\", \"state\":\"FINISHED\"}",
                    "\"id\":\"scsmbstgrx\", \"state\":\"FINISHED\"}",
                    "{\"id\":\"scsmbstgrx\", \"state\":\"FINISHED\"",
                    "{\"id\":\"scsmbstgrx\", \"state\":\"XXCXX\", \"timestamp\":1491377495218}",
                    "{\"id\":\"scsmbstgrx\", \"timestamp\":1491377495218}",
                    "{\"id\":\"scsmbstgrc\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}",
                    "{\"id\":\"scsmbstgrc\", \"state\":\"STARTED\", \"timestamp\":1491377495215}",
                    "{\"id\":\"scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}" )
                .collect( Collectors.toList() ) );
        final EventBundle bundle = processor.call();
        Assert.assertNotNull( bundle );
        Assert.assertEquals( 1, bundle.getComplete().size() );
        Assert.assertEquals( 1, bundle.getRemainder().size() );
        Assert.assertEquals( 6, bundle.getNumberOfInvalidEntries() );

    }
}
