package com.niltonrc.loganalysis.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niltonrc.loganalysis.exceptions.CheckException;
import com.niltonrc.loganalysis.exceptions.EventAnalysisFileNotFoundException;
import com.niltonrc.loganalysis.messages.EventAnalysisRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class EventServiceTest
{
    @InjectMocks
    private EventService eventService;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File createSmallFile() throws IOException
    {
        final File file = temporaryFolder.newFile( "small-file.txt" );
        try( final PrintWriter writer = new PrintWriter( file ) )
        {
            writer.println( "{\"id\":\"scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}" );
            writer.println( "{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}" );
            writer.println( "{\"id\":\"scsmbstgrc\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}" );
            writer.println( "{\"id\":\"scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}" );
            writer.println( "{\"id\":\"scsmbstgrc\", \"state\":\"STARTED\", \"timestamp\":1491377495210}" );
            writer.println( "{\"id\":\"scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495216}" );
        }
        return file;
    }

    @Before
    public void setup()
    {
        eventService = new EventService( new EventConverter( new ObjectMapper() ), events -> {
//            events.forEach( e -> System.out.println( e.toString() ) );
//            System.out.println( events.size() );
            try
            {
                Thread.sleep( 500L );
            }
            catch( InterruptedException ignored )
            {
            }
            return events.size();
        } );

        MockitoAnnotations.initMocks( this );
    }

    @Test( expected = CheckException.class )
    public void test_NullEventAnalysisRequest_ShouldThrowAnException() throws Throwable
    {
        eventService.doAnalysis( null );
    }

    @Test( expected = CheckException.class )
    public void test_EventAnalysisRequestWithNullFilename_ShouldThrowAnException() throws Throwable
    {
        eventService.doAnalysis( new EventAnalysisRequest( null ) );
    }

    @Test( expected = CheckException.class )
    public void test_EventAnalysisRequestWithFilenameEmpty_ShouldThrowAnException() throws Throwable
    {
        eventService.doAnalysis( new EventAnalysisRequest( "" ) );
    }

    @Test( expected = EventAnalysisFileNotFoundException.class )
    public void test_EventAnalysisRequestWithFilenameWhichDoesntExist_ShouldThrowAnException() throws Throwable
    {
        eventService.doAnalysis( new EventAnalysisRequest( "obladi-oblada/crazy/place" ) );
    }

//    @Test
//    public void test() throws IOException, ServiceException
//    {
//        File smallFile = createSmallFile();
////        eventService.doAnalysis( new EventAnalysisRequest( smallFile.getAbsolutePath() ) );
////        System.out.println( new File( "." ).getAbsolutePath() );
//        eventService.doAnalysis( new EventAnalysisRequest( "../../credit-suisse-file-builder/logfile-large.txt" ) );
//    }

}
