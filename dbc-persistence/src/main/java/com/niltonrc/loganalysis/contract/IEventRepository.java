package com.niltonrc.loganalysis.contract;

import com.niltonrc.loganalysis.entities.event.EventEntity;

import java.util.List;

public interface IEventRepository
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
    long batchInsert( List< EventEntity > events );
}