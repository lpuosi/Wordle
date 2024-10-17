package Server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.stream.*;
import com.google.gson.*;
import java.util.Properties;



public class ServerMain{
    public static final String configFile = "Server/file/server.properties";
    public static int port;
    public static ServerSocket listener;
    public static int Terminationdelay;
    public static String filename;
    public static String wordfile;
    public static AtomicReference<String> SecretWord;
    public static HashSet<String> vocabulary;
    public static AtomicInteger idgame;
    public static int wordTime;
    public static int udpPort;
    public static String udpAddress;

    public static void main(String[] args){
        
        try{readConfig();}catch(Exception e){e.printStackTrace();System.out.println("Errore caricamento properties"); return;}

        ConcurrentHashMap<String, User> login = new  ConcurrentHashMap<String, User>();
        vocabulary = new HashSet<String>();
        idgame = new AtomicInteger();
        //ripristino della struttura al riavvio del server
        ripristinoStato(login);
        //carico le parole dal file
        loadwords();
        //seleziono la parola segreta
        SecretWord = new AtomicReference<String>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try{
            scheduler.scheduleAtFixedRate(new WordExtractor(vocabulary, SecretWord, idgame), 0, wordTime, TimeUnit.MINUTES);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        try{
            listener = new ServerSocket(port);
            System.out.println("Server avviato");
            ExecutorService pool = Executors.newCachedThreadPool();
            Runtime.getRuntime().addShutdownHook(new PersistanceHandler(Terminationdelay, pool, listener, login,idgame,filename));                         //handler per la serializzazione
            while(true){
                try{
                    pool.execute(new ServerThread(listener.accept(),login, SecretWord, vocabulary, idgame,udpAddress,udpPort));
                }catch(SocketException e){                                                                                                                  //Il socket viene chiuso dal PersistanceHandler e al prossimo tentativo di accederci viene lanciata l'eccezione che fa uscire dal ciclo e terminare l'esecuzione
                    break;
                }
                
            }
        
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }

    public static void readConfig() throws FileNotFoundException, IOException {                         //metodo per leggere i parametri di configurazione dal file 
		InputStream input = new FileInputStream(configFile);
		Properties prop = new Properties();
		prop.load(input);
		port = Integer.parseInt(prop.getProperty("port"));
		Terminationdelay = Integer.parseInt(prop.getProperty("maxDelay"));
        filename = prop.getProperty("login");
        wordfile = prop.getProperty("words");
        wordTime = Integer.parseInt(prop.getProperty("period"));
        udpAddress = prop.getProperty("udpaddress");
        udpPort = Integer.parseInt(prop.getProperty("udpport"));
		input.close();
	}

    public static void ripristinoStato(ConcurrentHashMap<String,User> map){                 //lettura dello stato precendente dal file json
        JsonReader reader;
        Gson gson = new GsonBuilder().create();
        try{
            reader = new JsonReader(new FileReader(filename));
            reader.beginArray();                                                            //inizio array
            reader.beginObject();                                                           //inizio oggetto di oggetti utente
            while(reader.hasNext()){                                                        
                String username = reader.nextName();
                User user = gson.fromJson(reader, User.class);
                user.setOnline();                                                           //setto l'utente offline
                map.put(username, user);                                                    //inserisco l'utente nella struttura dati condivisa
            }
            reader.endObject();
            reader.beginObject();                                                           //oggetto contentente l'ultimo id del server
            reader.nextName();
            idgame.set(reader.nextInt());                                                   //scrivo l'id dell'ultimo gioco
            reader.endObject();
            reader.endArray();
            reader.close();

        }catch(FileNotFoundException e){
            System.out.println("Stato precendente non trovato");
            return;
        }catch(Exception e){
            e.printStackTrace();
        }


    }
    public static void loadwords(){                                                          //metodo per caricare le parole presenti sul file nel vocabolario
      try (BufferedReader br = new BufferedReader(new FileReader(wordfile))) {
            String word;
            while ((word = br.readLine()) != null) {
                vocabulary.add(word); // Aggiunge ogni parola al vocabolario
            }
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento del vocabolario.");
            e.printStackTrace();
        }  

    }
}

