/*
* Author: Beau Derrick and Christian Salano
* Date: 5/10/17
*/
package a3;

/**
 *
 * @author Beau Derrick
 */

import sage.event.*;

public class DeathEvent extends AbstractGameEvent
{
    int deaths;
    
    public DeathEvent (int s)
    {
        deaths = s;
       
    }
    
    public int getScore(){return deaths;}
}
