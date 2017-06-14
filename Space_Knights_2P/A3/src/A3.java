/*
* Author: Beau Derrick and Christian Salano
* Date: 5/10/17
*/
package a3;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class A3 
{
     public static InetAddress ip;
     public boolean isF;
   
    public static void main(String[] args) throws UnknownHostException 
    {
        ip = InetAddress.getLocalHost();
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter 1 for FullScreen:");
        int n = reader.nextInt(); // Scans the next token of the input as an int.
        boolean isF;
        if (n == 1)
            isF = true;
        else
            isF = false;
        MyGame game = new MyGame(ip.getHostAddress(), 2018, isF);
        game.start();
        
    }  
}
