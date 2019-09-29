package com.niltonrc.loganalysis.controller;

import com.niltonrc.loganalysis.contract.IEventService;
import com.niltonrc.loganalysis.messages.EventAnalysisRequest;
import com.niltonrc.loganalysis.messages.EventAnalysisResponse;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LogAnalysisControllerTest
{
    @Test
    public void testRunnerWhenItIsOk() throws Exception
    {
        final IEventService eventService = Mockito.mock( IEventService.class );
        when( eventService.doAnalysis( any( EventAnalysisRequest.class ) ) )
                .thenReturn( new EventAnalysisResponse( true, null ) );
        final LogAnalysisController controller = new LogAnalysisController( eventService );
        final ApplicationArguments arguments = Mockito.mock( ApplicationArguments.class );
        when( arguments.getSourceArgs() )
                .thenReturn( new String[]{ "-f", "filename" } );
        controller.getRunner().run( arguments );
        verify( arguments, times( 1 ) )
                .getSourceArgs();
        verify( eventService, times( 1 ) )
                .doAnalysis( any( EventAnalysisRequest.class ) );
    }

    @Test
    public void testRunnerWhenItIsNotOk() throws Exception
    {
        final IEventService eventService = Mockito.mock( IEventService.class );
        when( eventService.doAnalysis( any( EventAnalysisRequest.class ) ) )
                .thenReturn( new EventAnalysisResponse( true, null ) );
        final LogAnalysisController controller = new LogAnalysisController( eventService );
        final ApplicationArguments arguments = Mockito.mock( ApplicationArguments.class );
        when( arguments.getSourceArgs() )
                .thenReturn( new String[]{ "-asdasdasd" } );
        controller.getRunner().run( arguments );
        verify( arguments, times( 1 ) )
                .getSourceArgs();
        verify( eventService, times( 0 ) )
                .doAnalysis( any( EventAnalysisRequest.class ) );
    }

}
