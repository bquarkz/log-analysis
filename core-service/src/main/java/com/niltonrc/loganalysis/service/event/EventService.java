package com.niltonrc.loganalysis.service.event;

import com.niltonrc.loganalysis.constants.DBConstants;
import com.niltonrc.loganalysis.constants.GeneralConstants;
import com.niltonrc.loganalysis.contract.IEventRepository;
import com.niltonrc.loganalysis.contract.IEventService;
import com.niltonrc.loganalysis.dtos.AnalysisReportDto;
import com.niltonrc.loganalysis.event.Event;
import com.niltonrc.loganalysis.event.EventManager;
import com.niltonrc.loganalysis.exceptions.CheckException;
import com.niltonrc.loganalysis.exceptions.EventAnalysisFileNotFoundException;
import com.niltonrc.loganalysis.exceptions.ServiceException;
import com.niltonrc.loganalysis.messages.EventAnalysisRequest;
import com.niltonrc.loganalysis.messages.EventAnalysisResponse;
import com.niltonrc.loganalysis.utils.PlainInputStreamReader;
import com.niltonrc.loganalysis.utils.EventQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class EventService
    implements IEventService
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final Logger LOGGER = LoggerFactory.getLogger( EventService.class );
    private static final int DEFAULT_BATCH_SIZE = 3000;
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final EventConverter eventConverter;
    private final IEventRepository eventRepository;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public EventService(
            EventConverter eventConverter,
            IEventRepository eventRepository )
    {
        this.eventConverter = eventConverter;
        this.eventRepository = eventRepository;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Factories
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters And Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Logger getLogger()
    {
        return LOGGER;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EventAnalysisResponse doAnalysis( EventAnalysisRequest request ) throws ServiceException
    {
        try
        {
            if( request == null )
            {
                throw new CheckException( "request should not be null" );
            }

            if( StringUtils.isEmpty( request.getFilename() ) )
            {
                throw new CheckException( "filename should not be null" );
            }

            final File inputFile = new File( request.getFilename() );
            if( !inputFile.exists() )
            {
                throw new EventAnalysisFileNotFoundException( request.getFilename() );
            }

            return _doAnalysis( inputFile, getBatchSize( request ), getThreadPoolSize( request ) );
        }
        catch( ServiceException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new ServiceException( ex );
        }
    }

    private int getThreadPoolSize( EventAnalysisRequest request )
    {
        if( request.getThreadPoolSize() == null ) return DEFAULT_THREAD_POOL_SIZE;
        if( request.getThreadPoolSize() <= 0 ) return DEFAULT_THREAD_POOL_SIZE;
        return request.getThreadPoolSize();
    }

    private int getBatchSize( EventAnalysisRequest request )
    {
        if( request.getBatchSize() == null ) return DEFAULT_BATCH_SIZE;
        if( request.getBatchSize() <= 0 ) return DEFAULT_BATCH_SIZE;
        return request.getBatchSize();
    }

    private EventAnalysisResponse _doAnalysis( File inputFile, int batchSize, int threadPoolSize ) throws IOException, InterruptedException, ExecutionException
    {
        final long start = System.currentTimeMillis();

        final EventQueue< Future< EventBundle > > queue = new EventQueue<>();
        final EventQueue< List< Event > > completeQueue = new EventQueue<>();
        final EventQueue< Map< String, EventManager > > remainderQueue = new EventQueue<>();

        final ExecutorService executorService = Executors.newFixedThreadPool( threadPoolSize + 3 );
        final Future< Long > numberOfEntryProblems = executorService
                .submit( new BundleSplitterProcessor( queue, completeQueue, remainderQueue ) );
        final Future< Long > numberOfRemainders = executorService
                .submit( new MergeRemainderProcessor( completeQueue, remainderQueue ) );
        final Future< Long > successfulEntries = executorService
                .submit( new StorageCompleteProcessor( DBConstants.DB_BATCH_SIZE, eventConverter, eventRepository, completeQueue ) );

        try( final PlainInputStreamReader reader = new PlainInputStreamReader( inputFile ) )
        {

            Optional< List< String > > jsonBatch;
            while( ( jsonBatch = reader.readNext( batchSize ) ).isPresent() )
            {
                queue.top( executorService.submit( new EventProcessor( eventConverter, jsonBatch.get() ) ) );
            }
            executorService.awaitTermination( GeneralConstants.TIME_TO_WAIT_IN_MS, TimeUnit.MILLISECONDS );
        }
        final long numberOfSuccessfulLogs = successfulEntries.get();
        final long numberOfProblems = numberOfEntryProblems.get() + numberOfRemainders.get();

        final long end = System.currentTimeMillis();

        final AnalysisReportDto report = new AnalysisReportDto( getProcessingTimeInSecs( start, end ), numberOfSuccessfulLogs, numberOfProblems );
        return new EventAnalysisResponse( true, report );
    }

    private long getProcessingTimeInSecs(
            long start,
            long end )
    {
        return ( ( end - start ) - GeneralConstants.TIME_TO_WAIT_IN_MS ) / 1000;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
