import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

public class Client
{
	private static final int PORTServer = 9876;
	private DatagramSocket clientSocket = null;
	private byte[] sendData = null;
	private byte[] receiveData = null;
	private int size = 200;
	private DatagramPacket sendPacket = null;
	private DatagramPacket receivePacket = null;
	private InetAddress host = null;
	
	private String myName = "client";
	// private int counterMsg = 0;		optional use
	private String receivedMsg = "";
	private int myColor = 0;
	private World world = null;
	private static int scoreWhite = 0;
	private static int scoreBlack = 0;
	private int delay = 10;		// never set it to 0
	
	public Client()
	{
		// initialization of the fields
		try
		{

			clientSocket = new DatagramSocket();
			sendData = new byte[size];
			receiveData = new byte[size];
			
			host = InetAddress.getLocalHost();
			
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			sendPacket = new DatagramPacket(sendData, sendData.length, host, PORTServer);
		}
		catch(SocketException | UnknownHostException e)
		{
			// print the occurred exception
			System.out.println(e.getClass().getName() + " : " + e.getMessage());
		}
		
		// add a random number from 0 to 19 at the end of the name
		Random ran = new Random();
		int x = ran.nextInt(20);
		myName += x;
		
		// create the world of the game
		world = new World();
	}
	
	private void sendName()
	{
		// add your name here, remove comment below
		// myName = "your_name"
		
		try
		{
			// turn my name into bytes
			sendData = myName.getBytes("UTF-8");
			sendPacket.setData(sendData);
			sendPacket.setLength(sendData.length);
			clientSocket.send(sendPacket);
		}
		catch(IOException e)
		{
			// print the occurred exception
			System.out.println(e.getClass().getName() + " : " + e.getMessage());
		}
	}
	
	private void receiveMessages()
	{
		// keep on receiving messages and act according to their content
		while(true)
		{
			try
			{
				// waiting for a message from the server
				clientSocket.receive(receivePacket);
				
				// counterMsg++;
				
				// get the String of the message
				// no need to check for IPAddress and Port of sender, it must be the server of TUC-CHESS
				receivedMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
				
				System.out.println("Received message from server : " + receivedMsg);
				
				// get the first letter of the String
				String firstLetter = Character.toString(receivedMsg.charAt(0));
				
				if(firstLetter.equals("P"))		// received information is about my colour
				{
					// get the second letter of the String
					String secondLetter = Character.toString(receivedMsg.charAt(1));
					
					if(secondLetter.equals("W"))
						myColor = 0;
					else
						myColor = 1;
					
					world.setMyColor(myColor);
				}				
				else if(firstLetter.equals("G"))	// received information is about the game (begin/end)
				{
					// get the second letter of the String
					String secondLetter = Character.toString(receivedMsg.charAt(1));
					
					if(secondLetter.equals("B"))
					{
						// beginning of the game
						if(myColor == 0)
						{
							String action = world.selectAction();
							
							try
							{
								synchronized(this)
								{
									this.wait(delay);
								}
							}
							catch(InterruptedException e)
							{
								System.out.println(e.getClass().getName() + " : " + e.getMessage());
							}
							
							sendData = action.getBytes("UTF-8");
							sendPacket.setData(sendData);
							sendPacket.setLength(sendData.length);
							clientSocket.send(sendPacket);
						}
						else
							continue;
					}
					else	// secondLetter.equals("E") - the game has ended
					{
						scoreWhite = Integer.parseInt(Character.toString(receivedMsg.charAt(2))
								                    + Character.toString(receivedMsg.charAt(3)));
						
						scoreBlack = Integer.parseInt(Character.toString(receivedMsg.charAt(4))
						                            + Character.toString(receivedMsg.charAt(5)));
						
						if(scoreWhite - scoreBlack > 0)
						{
							if(myColor == 0)
								System.out.println("I won! " + scoreWhite + "-" + scoreBlack);
							else
								System.out.println("I lost. " + scoreWhite + "-" + scoreBlack);
							
							System.out.println("My average branch factor was : " + world.getAvgBFactor());
						}
						else if(scoreWhite - scoreBlack < 0)
						{
							if(myColor == 0)
								System.out.println("I lost. " + scoreWhite + "-" + scoreBlack);
							else
								System.out.println("I won! " + scoreWhite + "-" + scoreBlack);
							
							System.out.println("My average branch factor was : " + world.getAvgBFactor());
						}
						else
						{
							System.out.println("It is a draw! " + scoreWhite + "-" + scoreBlack);
							
							System.out.println("My average branch factor was : " + world.getAvgBFactor());
						}
							
						break;
					}
				}
				else	// firstLetter.equals("T") - a move has been made
				{
					// decode the rest of the message
					int nextPlayer = Integer.parseInt(Character.toString(receivedMsg.charAt(1)));
					
					int x1 = Integer.parseInt(Character.toString(receivedMsg.charAt(2)));
					int y1 = Integer.parseInt(Character.toString(receivedMsg.charAt(3)));
					int x2 = Integer.parseInt(Character.toString(receivedMsg.charAt(4)));
					int y2 = Integer.parseInt(Character.toString(receivedMsg.charAt(5)));
					
					int prizeX = Integer.parseInt(Character.toString(receivedMsg.charAt(6)));
					int prizeY = Integer.parseInt(Character.toString(receivedMsg.charAt(7)));
					
					scoreWhite = Integer.parseInt(Character.toString(receivedMsg.charAt(8)) 
							                      + Character.toString(receivedMsg.charAt(9)));
					
					scoreBlack = Integer.parseInt(Character.toString(receivedMsg.charAt(10)) 
												  + Character.toString(receivedMsg.charAt(11)));
					
					world.makeMove(x1,y1,x2,y2,prizeX,prizeY);
					
					if(nextPlayer==myColor)
					{
						String action = world.selectAction();
						
						try
						{
							synchronized(this)
							{
								this.wait(delay);
							}
						}
						catch(InterruptedException e)
						{
							System.out.println(e.getClass().getName() + " : " + e.getMessage());
						}
						
						sendData = action.getBytes("UTF-8");
						sendPacket.setData(sendData);
						sendPacket.setLength(sendData.length);
						clientSocket.send(sendPacket);			
					}
					else
					{
						continue;
					}				
				}
				
			}
			catch(IOException e)
			{
				System.out.println(e.getClass().getName() + " : " + e.getMessage());
			}
		}
	}
	
	public static int getScoreWhite()
	{
		return scoreWhite;
	}
	
	public static int getScoreBlack()
	{
		return scoreBlack;
	}
	
	// testing
	public static void main(String[] args)
	{
		Client client = new Client();
		
		// optionally adding delay to response
		if(args.length == 1)
			client.delay = Integer.parseInt(args[0]);
		
		// send the first message - my name
		client.sendName();
		
		// start receiving messages;
		client.receiveMessages();
	}

}
