import java.net.*;
import java.io.*;

public class UDPClient{

  // Variables de clase
  String message;
  String host;
  int serverPort;

  // Constructor
  public void UDPClient(String message,String host,int serverPort){
    this.message = message;
    this.host = host;
    this.serverPort = serverPort;
  }


  public void ejecutaUDPClient(){  

    // Para enviar indefinidamente informacion al servidor
    while(true){

      DatagramSocket aSocket = null;

      try {
        aSocket = new DatagramSocket();            
        String myMessage = message;
        byte [] m = myMessage.getBytes();

        // IP y puerto del servidor a donde se conectara este socket
        InetAddress aHost = InetAddress.getByName(host);                  

        DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
        aSocket.send(request);                              

        byte[] buffer = new byte[1000];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length); 
        aSocket.receive(reply); 
        System.out.println("Reply: " + (new String(reply.getData())).trim()  ); 
      }
      catch (SocketException e){
        System.out.println("Socket: " + e.getMessage());
      }
      catch (IOException e){
        System.out.println("IO: " + e.getMessage());
      }
      finally{
        if(aSocket != null) 
          aSocket.close();
      }
    }
  }


  // Metodo main, por si se requieren hacer pruebas dentro de esta clase
  public static void main(String args[]){ 

  }




}