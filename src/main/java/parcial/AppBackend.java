package parcial;

import java.io.IOException;

import parcial.server.Backend;

/**
 * Hello world!
 *
 */
public class AppBackend 
{
    public static void main( String[] args ) throws IOException
    {
        Backend backend = new Backend(35000);
        backend.start();
    }
}
