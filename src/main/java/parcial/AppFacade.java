package parcial;

import java.io.IOException;

import parcial.server.Facade;

public class AppFacade {
    public static void main( String[] args ) throws IOException
    {
        Facade facade = new Facade(8080, 35000);
        facade.start();
    }
}
