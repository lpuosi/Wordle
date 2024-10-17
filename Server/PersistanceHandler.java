package Server;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//import com.google.gson.*;
import com.google.gson.stream.JsonWriter;


public class PersistanceHandler extends Thread {                        //obbligatorio estendere, implements non va
    private int delay;
    private ExecutorService pool;
    private ServerSocket socket;
    private String filename;
    private ConcurrentHashMap<String, User> map;
    private AtomicInteger idgame;

    public PersistanceHandler(int delay, ExecutorService pool, ServerSocket socket, ConcurrentHashMap<String, User> map, AtomicInteger ID,String filename){
        this.delay = delay;
        this.pool = pool; 
        this.socket = socket;
        this.filename = filename;
        this.map = map;
        this.idgame = ID;
    }


    public void run(){
        System.out.println("\nServer shutting down..");

        try {socket.close();}
        catch (IOException e) {
        	System.err.printf("Errore server: %s\n", e.getMessage());
        }
        // Faccio terminare il pool di thread.
        pool.shutdown();
	    try {
	        if (!pool.awaitTermination(delay, TimeUnit.MILLISECONDS)) 
	        	pool.shutdownNow();
	    } 
	    catch (InterruptedException e) {pool.shutdownNow();}
        //salvataggio su file
        JsonWriter writer;
        try{
            writer = new JsonWriter(new FileWriter(filename));
            writer.beginArray();
            writer.beginObject();                                               //inizio la memorizzazione degli utenti
            for(String key : map.keySet()){

                writer.name(key);
                writer.beginObject();                                                           //inizio di un User
                writer.name("username").value(map.get(key).getUsername());
                writer.name("password").value(map.get(key).getPassword());
                writer.name("npartite").value(map.get(key).getNpartite());
                writer.name("partiteVinte").value(map.get(key).getpartiteVinte());
                writer.name("bestStreak").value(map.get(key).getBestStreak());
                writer.name("currentStreak").value(map.get(key).getCurrentStreak());
                writer.name("lastGame").value(map.get(key).getLastGame());
                
                writer.name("distribuzione").beginArray();                          //inizio della guess distribution
                for (int achievement : map.get(key).getDistribuzione()) {
                    writer.value(achievement);
                }
                writer.endArray();

                writer.endObject();
            }
            writer.endObject();
            writer.beginObject();                                                           //oggetto che contiene l'id dell'ultima partita (Del server)
            writer.name("idgame").value(idgame.get());
            writer.endObject();
            writer.endArray();
            System.out.println("Scrittura completata");
            writer.close();


        }catch(Exception e){
            e.printStackTrace();
        }
        
        //stato salvato
        System.out.println("Server offline");

    }
}
