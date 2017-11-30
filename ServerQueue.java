import java.util.concurrent.TimeUnit;
import java.util.*;
import java.io.*;


public class ServerQueue implements Runnable {

   private Thread t;
   private String threadName;
   static String[] argumentos;
   private String var;

   static Map<String, List<String>> resultados = new HashMap<String, List<String>>();


   // Constructor   
   ServerQueue(String name) {
      threadName = name;
      System.out.println("Creando el thread: " + threadName);
   }
   
   
   public void run() {

      System.out.println("Ejecutando " + threadName + " ..." );
      
      UDPClient queue = new UDPClient();

      resultados = queue.recibirPaquetesQueue();   

      // Java 7 - compliant
      try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("paquetes_recibidos_dispositivos.txt"), "utf-8"))) {

        // Se escriben las estadisticas de los paquetes procesados por la queue
        for (Map.Entry entry : resultados.entrySet()) {
            writer.write(entry.getKey() + ", " + entry.getValue() + "\n"); 
            //System.out.println(entry.getKey() + ", " + entry.getValue() + "\n"); // Para depurar
        }        
          writer.close();
      }
      catch(Exception e){}
        
      
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
   

   public static void main(String args[]) {

       argumentos = args;

       String nombreThread;
       ServerQueue serverQueue;
       
       // Este es un solo thread, porque se simula una sola queue 
       nombreThread = "Queue";
       serverQueue = new ServerQueue(nombreThread);
       serverQueue.start();    

       // Para permitir la sincronizacion entre variables de los threads
       try{
          TimeUnit.MICROSECONDS.sleep(1500000); // Medio segundo
       }
         catch(Exception e){}    

     }
        
   } // End-Class


