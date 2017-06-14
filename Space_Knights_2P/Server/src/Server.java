/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;

public class Server
{ 
	public static GameServerTCP testTCPServer;
	public static void main(String[] args) throws IOException
 { GameServerTCP testTCPServer = new GameServerTCP(2018); }
}

