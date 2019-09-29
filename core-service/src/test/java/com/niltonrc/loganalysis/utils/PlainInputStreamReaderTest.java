package com.niltonrc.loganalysis.utils;

import com.niltonrc.loganalysis.utils.PlainInputStreamReader;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.List;
import java.util.Optional;

public class PlainInputStreamReaderTest
{
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

    @Test
    public void test() throws IOException
    {
        try( final PlainInputStreamReader reader = new PlainInputStreamReader( createSmallFile() ) )
        {
            int counter = 0;
            Optional< List< String > > opt;
            while( ( opt = reader.readNext( 1 ) ).isPresent() )
            {
                Assert.assertNotNull( opt.get() );
                counter++;
            }
            Assert.assertEquals( 6, counter );
        }
    }

}
