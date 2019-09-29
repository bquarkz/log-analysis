package com.niltonrc.loganalysis.controller;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.niltonrc.loganalysis.contract.IEventService;
import com.niltonrc.loganalysis.exceptions.ServiceException;
import com.niltonrc.loganalysis.messages.EventAnalysisRequest;
import com.niltonrc.loganalysis.messages.EventAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class LogAnalysisController
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final Logger LOGGER = LoggerFactory.getLogger( LogAnalysisController.class );

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final IEventService eventService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    protected LogAnalysisController( IEventService eventService )
    {
        this.eventService = eventService;
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
    public ApplicationRunner getRunner()
    {
        return ( ApplicationArguments arguments ) ->
        {
            final LogAnalysisArguments logAnalysisArguments = new LogAnalysisArguments();
            JCommander commandParser = JCommander.newBuilder().addObject( logAnalysisArguments ).build();
            try
            {
                commandParser.parse( arguments.getSourceArgs() );
                final EventAnalysisResponse response = eventService.doAnalysis( buildRequest( logAnalysisArguments ) );
                getLogger().info( "RESPONSE: " + response );
            }
            catch( ParameterException ex )
            {
                System.out.println( "command wrong, please follow a short help for it:" );
                commandParser.usage();
            }
            catch( ServiceException ex )
            {
                getLogger().error( "internal problem", ex );
            }
            catch( Exception ex )
            {
                getLogger().error( "error from outer space -- really?", ex );
            }
            finally
            {
                System.exit( 0 );
            }
        };
    }

    public EventAnalysisRequest buildRequest( LogAnalysisArguments args )
    {
        return new EventAnalysisRequest(
                args.getFilename(),
                args.getBatchSize(),
                args.getThreadPoolSize(),
                args.isGz() );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
