package Client;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

public class ClientMain {
    public static String address;                   
    public static int port;
    public static String configFile = "Client/file/client.properties";
    private static String udpAddress;
    private static int udpPort;
    //struttura dati udp
    public static ConcurrentLinkedQueue<String> playerStats;
    public static void main(String[] args){
        playerStats = new ConcurrentLinkedQueue<String>();              //forse va resettata dopo il logout
        //carico i parametri dal file client.properties
        try{readConfig();}catch(Exception e){e.printStackTrace();System.out.println("Errore caricamento properties"); return;}
        
        while(true){                    //per il logout
            playerStats.clear();        //svuoto la lista (necessario per non mantenere le statistiche ricevute dopo il logout)
            Scanner UserInput = null;
            Scanner in = null;
            Socket socket;
            PrintWriter out = null;
            
            
            System.out.println("Benvenuto in Wordle, seleziona una delle seguenti azioni");
            System.out.println("1-Registrazione nuovo utente\n2-Login Utente\n3-Esci");
            //gestione login-registrazione
            try{
                UserInput = new Scanner(System.in);                         //scanner da tastiera
                while(true){
                    try{switch(Integer.parseInt(UserInput.nextLine())){
                        case 1:
                            socket = new Socket(address, port);  
                            in = new Scanner(socket.getInputStream());
                            out = new PrintWriter(socket.getOutputStream(), true);
                            out.println("registrazione");                               //preparo il server per la registrazione
                            while(true){
                                String mess = in.nextLine();
                                if(Objects.equals(mess, "Input-request")){
                                    String response = UserInput.nextLine();
                                    if(Objects.equals(response, "exit"))                //magari chiudi meglio il socket        //LEVALO
                                        return;
                                    out.println(response);
                                }else {
                                    if(Objects.equals(mess, "OK")){
                                        break;
                                    }else 
                                        System.out.println(mess);
                                }
                            }
                            System.out.println("Registrazione effettuata con successo!");
                            
                            break;
                        case 2:
                            socket = new Socket(address, port);  
                            in = new Scanner(socket.getInputStream());
                            out = new PrintWriter(socket.getOutputStream(), true);
                            out.println("login");

                            while(true){
                                String mess = in.nextLine();
                                if(Objects.equals(mess, "Input-request")){
                                    String response = UserInput.nextLine();
                                    if(Objects.equals(response, "exit"))            //LEVALO (e testa)
                                        return;
                                    out.println(response);
                                }else if(Objects.equals(mess, "OK")){
                                    break;
                                }else 
                                    System.out.println(mess);
                            }    
                            System.out.println("Login effettuato con successo!");
                            break;
                        case 3:
                            return;    
                        default:
                            System.out.println("Selezione non valida, riprova");
                            continue;
                    }
                    break;
                }catch(NumberFormatException e){
                    System.out.println("Selezione non valida, riprova");
                    continue;
                }
            }


            }catch(ConnectException e){
                System.out.println("Server offline, riprova più tardi");
                UserInput.close();
                return;
            }
            
            catch(Exception e){
                e.printStackTrace();
                return;
            }
            //login effettuato
            //creo un socket UDP
            MulticastSocket udpSocket = null;
            try{
            udpSocket= new MulticastSocket(udpPort);       
            //creo e attivo un thread per la ricezione dei pacchetti UDP 
            ClientThread udpHandler = new ClientThread(udpAddress,udpSocket,playerStats);           
            udpHandler.start();
            
            }catch(IOException e){System.out.println("Comunicazione con il server mancante, statistiche utenti non disponibili");}
            
            Boolean stato = false;              //false continuo a giocare, true faccio logout e torno alla prima schermata
            while(true){
                System.out.println("Scegli tra una delle seguenti azioni:\n1- Gioca\n2- Controlla statistiche\n3- Condividi\n4- Mostra altri utenti\n5- Logout");
            try{switch(Integer.parseInt(UserInput.nextLine())){
                    case 1:
                        gioca(UserInput,in, out);
                
                        break;
                    case 2:
                        out.println("send-statistic");
                        while(true){
                            String mess = in.nextLine();
                            if(Objects.equals(mess, "quit")){
                                break;
                            }else{
                                System.out.println(mess);
                            }
                        }
                        break;
                    case 3:
                        out.println("share");
                        String mess = in.nextLine();
                        if(!Objects.equals(mess, "OK")){
                            System.out.println(mess);
                        }
                        break;
                    case 4:
                        if(playerStats.isEmpty() == true){
                            System.out.println("Non sono presenti statistiche condivise");
                            break;
                        }
                        for(String stats: playerStats){
                            System.out.println(stats);
                        }
                        break;
                    case 5:
                        out.println("logout");
                        in.nextLine();              //riceve ok dal server
                        stato = true;               //necessario per uscire dal menù principale e tornare alla fase di registrazione/login
                        try{udpSocket.close();}catch(Exception e){e.printStackTrace();}
                        System.out.println("Disconnessione effettuata con successo");
                    
                        break;    
                    default:
                        System.out.println("Selezione non valida, riprova");
                    

            }
            }catch(NumberFormatException e){
                System.out.println("Selezione non valida, riprova");
                continue;
            }
            catch(NoSuchElementException e){
                System.out.println("Server offline, riprova più tardi");
                try{udpSocket.close();}catch(Exception f){f.printStackTrace();} 
                return; 
            }
            if(stato == true)           //richiesta di logout
                break;
        }
        //while esterno
        
    
    }

    }
    private static void gioca (Scanner UserInput, Scanner in, PrintWriter out){
        out.println("gioca");
        while(true){                        //controllo se può giocare
            String mess = in.nextLine();
            if(Objects.equals(mess, "quit")){
                return;
            }
            if(Objects.equals(mess, "OK")){
                break;
            }
            System.out.println(mess);
        }
       //allineati con tentativi = 12
        
        while(true){
            try{
            System.out.println("Scegli tra una delle seguenti azioni\n1- Invio parola\n2- Torna al menù principale");
            switch(Integer.parseInt(UserInput.nextLine())){
                case 1:                 //invio parola
                    out.println("Ready");
                    while(true){
                        String mess = in.nextLine();
                        if(Objects.equals(mess, "Input-request")){
                            out.println(UserInput.nextLine());
                            continue;
                       }
                       if(Objects.equals(mess, "quit")){
                        return;
                       }
                       if(Objects.equals(mess, "next")){
                        break;
                       }
                       System.out.println(mess); 
                       
                    }
                    break;
                case 2:                         //ritorno al menù principale
                    System.out.println("Perderai tutti i tentativi rimasti, continuare? y/n");
                    if(Objects.equals(UserInput.nextLine(), "y")){
                        out.println("menu");
                        in.nextLine();                  //messaggio di quit dal server
                        return;
                    }
                    else
                        break;
                default:
                    System.out.println("Selezione non valida, riprova");
            }
        }catch(NumberFormatException e){
            System.out.println("Selezione non valida, riprova");
            continue;
            }
        }
    }
    public static void readConfig() throws FileNotFoundException, IOException {
		InputStream input = new FileInputStream(configFile);
		Properties prop = new Properties();
		prop.load(input);
		port = Integer.parseInt(prop.getProperty("port"));
		address = prop.getProperty("address");
        udpAddress = prop.getProperty("udpAddress");
        udpPort = Integer.parseInt(prop.getProperty("udpPort"));
		input.close();
	}

    

}
