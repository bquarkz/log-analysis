package com.niltonrc.loganalysis.service.event;

import com.niltonrc.loganalysis.event.Event;
import com.niltonrc.loganalysis.event.EventManager;

import java.util.List;
import java.util.Map;

public interface EventBundle
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Default Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Contracts
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    long getNumberOfInvalidEntries();
    List< Event > getComplete();
    Map< String, EventManager > getRemainder();
}