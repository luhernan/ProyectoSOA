public class UserAPPS implements Runnable {
   static final int MAX_USER_APPS_THREADS = 40;

   private Thread t;
   private String threadName;

   private String var;
   private String mensaje;
   private String host;
   private int puerto;

   
   // Constructor
   UserAPPS(String name) {
      threadName = name;
      System.out.println("Creando el thread: " +  threadName);
   }
   

   public void run() {

      System.out.println("Ejecutando " +  threadName );

      mensaje = "HelloFromThread " + threadName;
      host = "localhost";
      puerto = 6789;

      UDPClient cliente = new UDPClient();
      cliente.UDPClient(mensaje,host,puerto);
      cliente.ejecutaUDPClient();


      // Desde aqui se puede dormir al thread actual
      try {
         Thread.sleep(5);
      }
      catch (InterruptedException e) {
         System.out.println("Thread " +  threadName + " interrumpido");
      }

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
      UserAPPS[] aplicaciones = new UserAPPS[MAX_USER_APPS_THREADS];


      for(int i=0; i < aplicaciones.length; i++){
         nombreThread = "Cliente" + i;
         aplicaciones[i] = new UserAPPS(nombreThread);
         aplicaciones[i].start();
      }

   } // End-Main


} // End-Class    
