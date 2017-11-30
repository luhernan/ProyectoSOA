import java.util.concurrent.TimeUnit;
import java.util.*;
import java.io.*;
//import java.io.Writer;



public class UserAPPS implements Runnable{
   static int MAX_USER_APPS_THREADS = 0;
   static final int MAX_USER_APPS_PACKETS = 200; // Para enviar 50 paquetes en total por aplicacion de usuario
   static final int CLIENT_TO_SERVER_QUEUE_PORT = 6789;

   static String[] argumentos;
   static int idCliente = 0;

   private Thread t;
   private String threadName;

   private String var;

   private int numPaquete = 1;

   int sumPaquetesEnviados = 0;

   static Map<String, List<String>> resultados = new HashMap<String, List<String>>();


   // Constructor
   UserAPPS(String name) {
      threadName = name;
      System.out.println("Creando el thread: " +  threadName);
   }
   
   
   public void run() {      

      System.out.println("Ejecutando " +  threadName );

      // Este mensaje se reescribe en el metodo "enviarPaquetesCliente"
      String mensaje = "Paquete enviado desde: " + threadName;
      String host = "localhost";
      // Todos las aplicaciones de cliente envian al mismo puerto del servidor que contiene la queue
      int puerto = CLIENT_TO_SERVER_QUEUE_PORT;

      UDPClient cliente_origen = new UDPClient();
      cliente_origen.UDPClient(mensaje,host,puerto);
      resultados = cliente_origen.enviarPaquetesCliente(idCliente);
            
      // Mensaje para determinar si un thread termina cuando debe
      System.out.println("Saliendo del thread " +  threadName + "\n");         
   
   }
   
   
   public void start () {
      System.out.println("Iniciando " +  threadName + " ...");
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
   
   // -c : # clientes
   // -p : quota cuando se realiza polling
   //java UserAPPS -c 3 -p 10
   public static void main(String args[]) {

      argumentos = args;
      MAX_USER_APPS_THREADS = Integer.parseInt(argumentos[1]);

      String nombreThread;
             
      UserAPPS[] aplicaciones = new UserAPPS[MAX_USER_APPS_THREADS];

      // Java 7 - compliant
      try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("paquetes_enviados_clientes.txt"), "utf-8"))) {
  
      for(int j=0; j < MAX_USER_APPS_PACKETS/MAX_USER_APPS_THREADS; j++) // # de tandas - MODIFICAR

      for(int i=0; i <= aplicaciones.length-1; i++){                  
       
         idCliente = i;
         nombreThread = "Cliente " + i;
         aplicaciones[i] = new UserAPPS(nombreThread);          
         // Se envian los paquetes desde las aplicaciones de cliente
         aplicaciones[i].start();  

         // Para permitir la sincronizacion entre variables de los threads
         try{
         TimeUnit.MICROSECONDS.sleep(500000); // Medio segundo
         }
         catch(Exception e){}

         // Se escriben las estadisticas de los paquetes enviados por el thread actual
         for (Map.Entry entry : resultados.entrySet()) {
            writer.write(entry.getKey() + ", " + entry.getValue() + "\n"); 
         }
                        
         // Para permitir la sincronizacion entre variables de los threads
         try{
         TimeUnit.MICROSECONDS.sleep(500000); // Medio segundo
         }
         catch(Exception e){}
      }
      writer.close();
      }
      catch(Exception e){}
      
     }

   } // End-Class
