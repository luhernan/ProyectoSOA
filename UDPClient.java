import java.net.*;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.time.*;

import java.time.format.DateTimeFormatter;


// Esta clase contiene operaciones comunes a un socket de tipo cliente/servidor
public class UDPClient{

  // Constantes
  int MAX_SIZE_QUEUE = 10;
  int SENDED_PACKAGES_PER_CLIENT = 0; // Toma su valor de la clase DevicesIO.java con el 5to argumento

  // Puerto destino desde la queue
  final int NEW_PORT_NUMBER = 6790;

  String[] parametros;


  // Para almacenar la informacion que trae cada paquete que llega a la cola
  byte[] data;
  int length;
  InetAddress address;
  int port;

  // Estructuras para almacenar las estadisticas de los paquetes
  Map<String, List<String>> hm = new HashMap<String, List<String>>();
  List<String> values = new ArrayList<String>();


  // Para simular una 'queue' que almacena paquetes
  //LinkedBlockingQueue<DatagramPacket> lbqueue = new LinkedBlockingQueue<DatagramPacket>(MAX_SIZE_QUEUE); 
  ArrayBlockingQueue<String> abqueue = new ArrayBlockingQueue<String>(MAX_SIZE_QUEUE);

  // Variables de clase
  String message;
  String host;
  int serverPort;
  int contador = 0;
  boolean envio = true;


  // Constructor
  public void UDPClient(String message,String host,int serverPort){    
    this.message = message;
    this.host = host;
    this.serverPort = serverPort;
  }

//************************************************************************************************************
//************************************************************************************************************
//************************************************************************************************************   


  // Metodo para simular el envio de paquetes UDP desde una aplicacion de cliente
  public Map enviarPaquetesCliente(int idCliente){  
    int numPaquete = 0;

    // Para leer los parametros que se reciben desde el metodo main
    parametros = UserAPPS.argumentos;

    // Parametro para cuando se realiza polling
    int limitClient = Integer.parseInt(parametros[3]);
    int numeroClientes = Integer.parseInt(parametros[1]);


    // Estructuras para almacenar las estadisticas de los paquetes
    Map<String, List<String>> hm = new HashMap<String, List<String>>();
    List<String> values = new ArrayList<String>();


    // Se envian tandas de # paquetes hasta alcanzar el numero total de paquetes por enviar - MODIFICAR
    while(contador < numeroClientes){
      values.clear();

      this.contador ++;
      numPaquete++;

      DatagramSocket aSocket = null;

      try {

        // Se genera un identificador unico para cada paquete, se utiliza en el mensaje
        // y como llave en el HashMap para las estadisticas
        String uniqueID = UUID.randomUUID().toString();

        aSocket = new DatagramSocket();
        String myMessage = uniqueID + ", Cliente " + idCliente;
        byte [] m = myMessage.getBytes();

        // IP del servidor a donde se conectara este socket
        InetAddress aHost = InetAddress.getByName(host);      

        // Paquete a enviar con la informacion correspondiente en su interior
        DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
        aSocket.send(request);

        LocalTime date = LocalTime.now();
        // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm:ss:SSS:AAAA:nnnnnnnnn");
        String text = date.format(formatter);

        // Se registra el tiempo en que se envia el paquete
        values.add(text);
        values.add("Cliente " + String.valueOf(idCliente));
        hm.put(uniqueID, values);

        // Para realizar polling
        if(numPaquete >= limitClient && limitClient != 0){
          // Verifica si existe mensaje para este thread
          verificaMensaje();
        }                                        

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
    return hm;
  }


//************************************************************************************************************
//************************************************************************************************************
//************************************************************************************************************   



  // Mensaje multicast
  private void verificaMensaje(){

    try{

      // El mismo puerto que se uso en la parte de enviar.
      MulticastSocket escucha = new MulticastSocket(55557);

      // Nos ponemos a la escucha de la misma IP de Multicast que se uso en la parte de enviar.
      escucha.joinGroup(InetAddress.getByName("230.0.0.1"));

      // Un array de bytes con tamaño suficiente para recoger el mensaje enviado, 
      // bastaría con 4 bytes.
      byte [] dato = new byte [1024];

      // Se espera la recepcion. La llamada a receive() se queda
      // bloqueada hasta que llegue un mesnaje.
      DatagramPacket dgp = new DatagramPacket(dato, dato.length);
      //escucha.receive(dgp);

      // Delay para simular que deje de enviar paquetes
      try{
        TimeUnit.MILLISECONDS.sleep(10);
      }
      catch(Exception e){}

    }  
    catch (IOException e){
      System.out.println("IO: " + e.getMessage());
    }

  }



//************************************************************************************************************
//************************************************************************************************************
//************************************************************************************************************   

  
  // Este metodo simula un servidor que almacena los paquetes que recibe en una 'queue' 
  public Map recibirPaquetesQueue(){

    int cont = 1;

    // Para leer los parametros que se reciben desde el metodo main
    parametros = ServerQueue.argumentos;
    int polling = Integer.parseInt(parametros[3]);
    int numClients = Integer.parseInt(parametros[1]);
    int MAX_USER_APPS_PACKETS = Integer.parseInt(parametros[5]);

    boolean resultado;
    DatagramSocket aSocket = null;

    try{
      // Este puerto es el mismo al que envian paquetes las aplicaciones de cliente
      int serverPort = 6789;
      aSocket = new DatagramSocket(serverPort); 
        byte[] buffer = new byte[1000]; // buffer encapsulará mensajes

        System.out.println("Waiting for messages ...\n"); 

        
        // Para que el servidor este "esperando" al menos tantos mensajes como la suma
        // de cada uno de los paquetes que enviaran los threads
        System.out.println("Iteracion limite: " + MAX_USER_APPS_PACKETS * numClients);

        long start = System.currentTimeMillis();        
        long end = start + 500*1000; // 60 seconds * 1000 ms/sec


        //while(cont <= UserAPPS.MAX_USER_APPS_PACKETS * numClients){ 
        while (System.currentTimeMillis() < end){
         cont++;

         end = start + 500*1000; // 500 seconds * 1000 ms/sec

       if(cont % 50 == 0){
        System.out.println("\nProcesando paquete... " + cont);
      }

      //System.out.println("El tamaño de la queue es: " + abqueue.size()); // Para depurar

      DatagramPacket request = new DatagramPacket(buffer, buffer.length);
      aSocket.receive(request);      

      if(aSocket == null){
        System.out.println("Respuesta nula");
        break;
      }

      data = request.getData();
      String str2 = new String(data);

       // Se mete a la queue el paquete que llego
      try{
        // Inserts the specified element at the tail of this queue if it is possible to do so immediately without exceeding the queue's capacity, returning true upon success and false if this queue is full.       
        abqueue.offer(str2);
      }
      catch(Exception e){}


       // EL POLLING SE ACTIVA AL 75% DE LA CAPACIDAD DE LA QUEUE Y SI ADEMAS ESTA ACTIVADA DICHA OPCION
       //if(abqueue.size() >= (MAX_SIZE_QUEUE*75)/100 && polling != 0){      
      if(polling != 0 && abqueue.size() >= 5){
        System.out.println("\nRealizando polling ...");
        //int contador = 0;

        // Notificar a los clientes que dejen de enviar paquetes cuando se activa el polling
        MulticastSocket enviador = new MulticastSocket();

        // El dato que queramos enviar en el mensaje, como array de bytes.
        byte [] dato = new byte[] {1,2,3,4};

        // Usamos la direccion Multicast 230.0.0.1, por poner alguna dentro del rango
        // y el puerto 55557, uno cualquiera que esté libre.
        DatagramPacket dgp = new DatagramPacket(dato, dato.length, InetAddress.getByName("230.0.0.1"), 55557);

        // Envío
        //enviador.send(dgp);

        enviarPaqueteQueue(5);
        abqueue.clear();             
      }       
       //System.out.println("Tamanio queue: " + abqueue.size());

       //if(abqueue.size() % SENDED_PACKAGES_PER_CLIENT == 0 && abqueue.size()>0){
      if(abqueue.size() >= 5){                            
        // Para enviar los paquetes desde la queue hacia el dispositivo E/S correspondiente      
        enviarPaqueteQueue(5);
        abqueue.clear();
      }
    }


  }

  catch (SocketException e){
    System.out.println("Socket: " + e.getMessage());
  }
  catch (IOException e) {
   System.out.println("IO: " + e.getMessage());
 }
 finally {
  if(aSocket != null) 
    aSocket.close();
}
return this.hm;
}


//************************************************************************************************************
//************************************************************************************************************
//************************************************************************************************************   



// Metodo para re-enviar un numero determinado de paquetes desde la 'queue'
public void enviarPaqueteQueue(int numPaquetes){

  DatagramSocket aSockete = null;

  LocalTime date;
  DateTimeFormatter formatter;
  String hora = null;
  byte[] mensajeRespuesta;
  String mensajeString = null;
  String uniqueID = null;
  String idCliente = null;


  // Contador para el numero de paquetes re-enviados
  int contador = 1;
  byte[] datos;
  String mensajeEnviar = null;

  // Para enviar un numPaquetes a los dispositivos de E/S
  while(contador <= numPaquetes){

    hora = null;
    uniqueID = null;
    idCliente = null;
    values.clear();

    try {        
      aSockete = new DatagramSocket();
      // Direccion IP del host a donde se enviaran los paquetes
      InetAddress aHost = InetAddress.getByName(host);                  

      try{
        // Se extrae un paquete de la queue
        mensajeEnviar = abqueue.take();          
      }
      catch(Exception e){}

      // Empaquetar el mensaje eliminado de la queue para re-enviarlo al dispositivo E/S correspondiente
      //System.out.print("\nElemento eliminado de la queue que se va a enviar: " + mensajeEnviar); // Para depurar
      byte [] mensajeBytes = mensajeEnviar.getBytes();        

      // Harcoded :(
      String digitoCliente = mensajeEnviar.substring(46, 47);       
      // Para calcular el ajuste que se le tiene que hacer al puerto a donde
      // se tiene que enviar este paquete
      int offset = Integer.parseInt(digitoCliente);                        

      // Este puerto no sirve, es el puerto desde el que se recibio el paquete
      //port = enviar.getPort();                                    

      // Paquete que se va a enviar al dispositivo E/S correspondiente
      DatagramPacket request = new DatagramPacket(mensajeBytes, mensajeBytes.length, aHost, NEW_PORT_NUMBER + offset);
      aSockete.send(request);   
      //System.out.println("\nEnviando paquete desde la queue ..."); // Para depurar

      // Para registrar la respuesta del mensaje anterior enviado
      byte[] buffer = new byte[1000];
      DatagramPacket reply = new DatagramPacket(buffer, buffer.length);        
      aSockete.receive(reply);

      // Se registra la hora en que se recibe la respuesta
      date = LocalTime.now();
      // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
      formatter = DateTimeFormatter.ofPattern("H:mm:ss:SSS:AAAA:nnnnnnnnn");
      hora = date.format(formatter);

      mensajeRespuesta = reply.getData();
      mensajeString = new String(mensajeRespuesta); 
      System.out.print("\nElemento recibido: " + mensajeEnviar + ", " + hora); // Para depurar       
      //System.out.println("(Reply) Contenido del mensaje enviado: " + mensajeString); // Para depurar

      // Harcoded :(
      uniqueID = mensajeString.substring(0, 36); //UUID
      idCliente = mensajeString.substring(46,47);

      // Se almacena en un registro la informacion anterior (HashMap)
      values.add(hora);
      values.add("Cliente " + idCliente);
      hm.put(uniqueID, values);        

      }
      catch (SocketException e){
        System.out.println("Socket: " + e.getMessage());
      }
      catch (IOException e){
        System.out.println("IO: " + e.getMessage());
      }
      finally{
        if(aSockete != null) 
          aSockete.close();
      }
      contador = contador + 1;            

    } // End-While

  } // End-Method "enviarPaqueteQueue"


//************************************************************************************************************
//************************************************************************************************************
//************************************************************************************************************   



  // Recibir paquetes (I/O Device)
  public void recibirPaquetesDestino(int puertoDispositivo){    

    DatagramSocket aSocket = null;
    SENDED_PACKAGES_PER_CLIENT = Integer.parseInt(DevicesIO.argumentos[5]);

    try{                    
      System.out.println("Puerto desde el servidor: " + puertoDispositivo);      
        aSocket = new DatagramSocket(serverPort); // Puerto definido en el constructor
        byte[] buffer = new byte[1000]; // buffer encapsulara mensajes


        // Para que los dispositivos de E/S esperen al menos tantos paquetes como los que se enviaron en un inicio
        while(contador <= SENDED_PACKAGES_PER_CLIENT * DevicesIO.MAX_DEVICES_THREADS){
          contador++;

          System.out.println("Waiting for messages IO Device...");             

          DatagramPacket request = new DatagramPacket(buffer, buffer.length);
          aSocket.receive(request);


          DatagramPacket reply = new DatagramPacket(request.getData(),request.getLength(),request.getAddress(),request.getPort());

          /*System.out.println("Server (IO Device) received a request from "+ request.getAddress());
          System.out.println("con la siguiente informacion: " + request.getData());
          System.out.println("con el siguiente puerto: " + request.getPort());
          System.out.println("desde el siguiente dispositivo E/S: " + puertoDispositivo);
          */
          
          aSocket.send(reply);
          //System.out.println("Se ha enviado la respuesta a la queue "); // Para depurar       
     }
   }
   catch (SocketException e){
    System.out.println("Socket: " + e.getMessage());
  }
  catch (IOException e) {
   System.out.println("IO: " + e.getMessage());
 }
 finally {
  if(aSocket != null) 
    aSocket.close();
}   

}


//************************************************************************************************************
//************************************************************************************************************
//************************************************************************************************************   


  // Metodo main, por si se requieren hacer pruebas dentro de esta clase
  public static void main(String args[]){ 

  }
}
