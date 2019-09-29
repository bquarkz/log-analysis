package com.niltonrc.loganalysis.utils;

import org.apache.commons.lang3.StringUtils;

import javax.swing.text.html.Option;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PlainInputStreamReader implements Closeable, AutoCloseable, Iterable
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int DEFAULT_BUFFER_SIZE = 4 * 8192;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final InputStream inputStream;
    private final Reader inputReader;
    private final BufferedReader bufferedReader;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public PlainInputStreamReader( File inputFile ) throws FileNotFoundException
    {
        this( inputFile, DEFAULT_BUFFER_SIZE );
    }

    public PlainInputStreamReader( File inputFile, int bufferSize ) throws FileNotFoundException
    {
        if( inputFile == null )
        {
            throw new IllegalArgumentException( "input file should not be null" );
        }

        this.inputStream = new FileInputStream( inputFile );
        this.inputReader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 );
        this.bufferedReader = new BufferedReader( inputReader, bufferSize <= 0 ? DEFAULT_BUFFER_SIZE : bufferSize );
    }

    @Override
    public Iterator iterator()
    {
        return buildIterator();
    }

    public Stream< String > stream()
    {
        final Iterator<String> iterator = buildIterator();
        final Spliterator< String > spliterator = Spliterators
                .spliteratorUnknownSize( iterator, Spliterator.ORDERED | Spliterator.NONNULL );
        return StreamSupport.stream( spliterator, false );
    }

    private Iterator< String > buildIterator()
    {
        return new Iterator< String >()
        {
            String nextLine = null;

            @Override
            public boolean hasNext()
            {
                if( nextLine != null )
                {
                    return true;
                }
                else
                {
                    try
                    {
                        nextLine = bufferedReader.readLine();
                    }
                    catch( IOException e )
                    {
                        return false;
                    }
                    return ( nextLine != null );
                }
            }

            @Override
            public String next()
            {
                if( nextLine != null || hasNext() )
                {
                    final String line = nextLine;
                    nextLine = null;
                    return line;
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    public Optional< List< String > > readNext( final int batch ) throws IOException
    {
        if( batch <= 0 ) throw new IllegalArgumentException( "batch should be bigger than 0" );
        final List< String > result = new ArrayList<>( batch );
        for( int i = 0; i < batch; i++ )
        {
            final String line = bufferedReader.readLine();
            if( line == null )
            {
                break;
            }
            if( !StringUtils.isEmpty( line ) || line.startsWith( "#" ) )
            {
                result.add( line );
            }
        }
        return result.isEmpty() ? Optional.empty() : Optional.of( result );
    }

    @Override
    public void close() throws IOException
    {
        bufferedReader.close();
        inputReader.close();
        inputStream.close();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
