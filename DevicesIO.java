import java.util.concurrent.TimeUnit;
import java.util.*;
import java.io.*;


public class DevicesIO implements Runnable {
   // El valor de esta constante debe tener el mismo valor en las aplicaciones de los clientes
   static int MAX_DEVICES_THREADS = 0;

   static String[] argumentos;
   
   static final int QUEUE_TO_DEVICE_PORT = 6790;

   private Thread t;
   private String threadName;
   private static int numPort = 0;

   private String var;

   // Constructor
   DevicesIO(String name, int numPort) {
      threadName = name;
      this.numPort = numPort;

      System.out.println("Creando el thread: " +  threadName);
   }
   
   
   public void run() {      

      System.out.println("Ejecutando " +  threadName );

      // Este mensaje se modifica despues
      String mensaje = "Paquete enviado desde: " + threadName;
      String host = "localhost";
      // Este puerto se actualiza conforme se tenga que enviar un paquete desde la queue      
      //int puerto = numPort;

      UDPClient cliente_destino = new UDPClient();
      cliente_destino.UDPClient(mensaje,host,numPort);
      cliente_destino.recibirPaquetesDestino(numPort);        

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

       String nombreThread;

       argumentos = args;

       MAX_DEVICES_THREADS = Integer.parseInt(argumentos[1]);
       
       int sum = 0;
       DevicesIO[] dispositivos = new DevicesIO[MAX_DEVICES_THREADS];

       numPort = QUEUE_TO_DEVICE_PORT;
       
      for(int i=0; i <= dispositivos.length-1; i++){   
         System.out.println("Puerto: " + numPort);               
         nombreThread = "Dispositivo E/S - " + i;
         dispositivos[i] = new DevicesIO(nombreThread, numPort);          
         // Se comienzan a enviar paquetes desde la queue a los dispositivos de E/S
         dispositivos[i].start();
                                 
        // Para dejar que se inicialice el 'dispositivo' correctamente antes de que cambie el puerto
        try{
          TimeUnit.MICROSECONDS.sleep(500000); // Medio segundo
        }
        catch(Exception e){         
        }

         numPort = numPort + 1;
      //}
      //      writer.close();

      }
      //catch(Exception e){}      
      }




     


  /*
   public static void main(String args[]) {
       //String mensaje = "HelloFromIO Device";
       //String host = "localhost";
       //int puerto = 6789;


      UDPClient cliente_destino = new UDPClient();
        

      // Thread que envia informacion
      Thread t1 = new Thread() {

            @Override
            public void run() {                                             

               // Para definir un mensaje, host y puerto
               
               cliente_destino.recibirPaquetesDestino();                                
            }
        };
        
        // Se comienzan a enviar paquetes desde las aplicaciones de cliente
        t1.start();       */ 

        /*
        try{
        TimeUnit.SECONDS.sleep(1);
        }
        catch(Exception e){         
        }
        */
   //  }





   } // End-Class






