package com.niltonrc.loganalysis.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niltonrc.loganalysis.dtos.LogEntryDto;
import com.niltonrc.loganalysis.entities.event.EventEntity;
import com.niltonrc.loganalysis.event.Entry;
import com.niltonrc.loganalysis.event.EntryState;
import com.niltonrc.loganalysis.event.Event;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventConverter
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final ObjectMapper mapper;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public EventConverter( ObjectMapper mapper )
    {
        this.mapper = mapper;
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
    private boolean validation( LogEntryDto logEntryDto )
    {
        if( logEntryDto == null ) return false;
        if( StringUtils.isEmpty( logEntryDto.getId() ) ) return false;
        if( logEntryDto.getTimestamp() == null || logEntryDto.getTimestamp() <= 0L ) return false;
        if( !EntryState.exist( logEntryDto.getState() ) ) return false;
        return true;
    }

    private Entry mapToEntry( LogEntryDto logEntryDto )
    {
        final Entry entry = new Entry();
        entry.setId( logEntryDto.getId() );
        entry.setState( EntryState.valueOf( logEntryDto.getState() ) );
        entry.setTimestamp( logEntryDto.getTimestamp() );
        entry.setHost( logEntryDto.getHost() );
        entry.setType( logEntryDto.getType() );
        return entry;
    }

    private LogEntryDto mapToLogEntryDto( String s )
    {
        if( StringUtils.isEmpty( s ) || StringUtils.startsWith( s, "#" ) )
        {
            return null;
        }

        try
        {
            return mapper.readValue( s, LogEntryDto.class );
        }
        catch( IOException e )
        {
            return null;
        }
    }

    public List< Entry > mapToEntries( List< String > jsons )
    {
        return jsons
                .stream()
                .map( this::mapToLogEntryDto )
                .filter( this::validation )
                .map( this::mapToEntry )
                .collect( Collectors.toList() );
    }

    public List< EventEntity > mapToEventEntities( List< Event > events )
    {
        return events
                .stream()
                .map( this::mapToEventEntity )
                .collect( Collectors.toList() );
    }

    public EventEntity mapToEventEntity( Event event )
    {
        final EventEntity entity = new EventEntity();
        entity.setId( event.getId() );
        entity.setDuration( event.getDuration() );
        entity.setHost( event.getHost() );
        entity.setType( event.getType() );
        entity.setAlert( event.isAlert() );
        return entity;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
