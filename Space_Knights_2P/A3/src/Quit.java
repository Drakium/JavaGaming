/*
* Author: Beau Derrick and Christian Salano
* Date: 5/10/17
*/
package a3;

/**
 *
 * An input action for exiting the application
 */
import net.java.games.input.Event;
import sage.input.action.*;

public class Quit extends AbstractInputAction 
{
    MyGame game;
    
    public Quit(MyGame g)
    {
        game = g;
    }
    
    public void performAction(float time, Event e)
    {
        game.shutdown();
        System.exit(0);
    }
    
}