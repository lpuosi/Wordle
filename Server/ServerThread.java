package Server;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ServerThread implements Runnable{
    private Socket socket;
    private ConcurrentHashMap<String, User> loginTable;
    private User utente;
    private int tentativi;
    private AtomicReference<String> SecretWord;         //parola da indovinare
    public HashSet<String> vocabulary;                  //contiene tutte le parole valide
    private AtomicInteger ServerGame;        //game del server (viene aggiornato quando il serve cambia parola)
    private int gamePlaying;                //gioco al quale il giocatore sta giocando
    private String currentWord;
    private String udpAddress;
    private int udpPort;
    private String latestAttempt;

    public ServerThread(Socket s, ConcurrentHashMap<String, User> login, AtomicReference<String> SecretWord, HashSet<String> vocabulary, AtomicInteger idgame,String udpAddress, int udpPort){
        this.socket = s;
        this.loginTable = login;
        this.SecretWord = SecretWord;
        this.ServerGame = idgame;
        gamePlaying = ServerGame.get();
        this.vocabulary = vocabulary;
        this.currentWord = SecretWord.get();
        this.udpAddress = udpAddress;
        this.udpPort = udpPort;
    }
    @Override
    public void run(){
        Boolean done = false;
        utente = null;
        try(Scanner in = new Scanner(socket.getInputStream()); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
            System.out.println("Giocatore "+Thread.currentThread().getId()+" connesso");  
            while(!done){
                switch(in.nextLine()){
                    case "registrazione":
                            while(true){ 
                                out.println("Inserisci username");
                                out.println("Input-request");
                                String username = in.nextLine();    
                                out.println("Inserisci la password");
                                out.println("Input-request");
                                String pass = in.nextLine();
                                //controlli
                                if(Objects.equals(pass, "") || Objects.equals(pass, " ")){
                                    out.println("Password non valida");
                                    continue;
                                }
                                User newUser = new User(username, pass);
                                if(loginTable.putIfAbsent(username, newUser) != null){                                                      //controllo se l'username è già stato utilizzato e in caso contrario inserisco atomicamente il nuovo utente
                                    out.println("Username già utilizzato");
                                    continue;
                                }else{  
                                    newUser.online.set(true);                                                                               //Dopo la registrazione l'utente entra subito nel gioco, quindi va settato online
                                    utente = loginTable.get(username);
                                    out.println("OK");
                                    break;
                                }
                            }
                        break;
                    case "login":   
                        while(login(in, out) == -1){        //finchè il login non è stato completato con successo non è possibile proseguire
                        ;
                        }
                        break;
                    default:
                            System.out.println("Fatal Error");
                        return;    
                }
                break;

            }

            //accessibile solo dopo login/registrazione effettuata con successo
            while(true){
              switch(in.nextLine()){
                    case "gioca":
                        gamePlaying = ServerGame.get();
                        currentWord = SecretWord.get();
                        if(utente.getLastGame() == gamePlaying){                        //controllo se il giocatore ha già tentato di indovinare la parola
                            out.println("Hai già giocato per questa parola, torna per la prossima");
                            out.println("quit");
                            break;
                        }else{
                            out.println("OK");
                        }
                        tentativi = 12;             //allineato con commento
                        utente.setLastGame(gamePlaying);        //per non poter giocare alla stessa parola
                        utente.setNpartite(utente.getNpartite()+1);
                        System.out.println("Pronto per giocare");               
                        utente.clearAttempts();
                    
                        String attempt = "";
                        attempt += "Wordle " +gamePlaying+": "+"TEMP\n";
                        while(tentativi > 0){   
                            if(Objects.equals(in.nextLine(), "menu")){    //il giocatore è tornato al menù principale                                    //finchè ha tentativi a disposizione
                                tentativi = 0;
                                break;
                            }
                            if(ServerGame.get() != gamePlaying){
                                out.println("La parola è cambiata, i tentativi verranno ripristinati, seleziona gioca per giocare ancora");
                                tentativi = 0;                  //necessario per l'if in fondo per azzerare la current streak
                                break;          //mi porta fuori dal while(torno al menù principale)
                            } 
                            out.printf("Prova a indovinare la parola, tentativi rimasti %d\n", tentativi);
                            out.println("Input-request");
                            String guessedWord = in.nextLine();
                            if(!vocabulary.contains(guessedWord) || guessedWord.length() != currentWord.length()){                                //controllo se la parola esiste nel vocabolario
                                out.println("La parola non esiste");
                                System.out.println("GuessedWord: "+guessedWord+"\nSecretWord: "+currentWord);
                                out.println("next");
                            }else{
                                if(Objects.equals(guessedWord, currentWord)){
                                    out.println("Complimenti hai indovinato la parola");
                                    tentativi--;
                                    //setta statistiche
                                    //partite vinte
                                    utente.setpartiteVinte(utente.getpartiteVinte()+1);
                                    //ultima streak
                                    utente.setCurrentStreak(utente.getCurrentStreak()+1);
                                    //best streak
                                    if(utente.getCurrentStreak() > utente.getBestStreak())      //se sto facendo il record
                                        utente.setBestStreak(utente.getCurrentStreak());
                                    //guess distribution
                                    int[] distr = utente.getDistribuzione();
                                    distr[12-tentativi-1]++;
                                    //attempts
                                    //utente.concatAttempts("+, +, +, +, +, +, +, +, +, +");
                                    attempt += "+, +, +, +, +, +, +, +, +, +";
                                    //fine set statistiche
                                    
                                    break;
                                }else{
                                    //creo l'indizio
                                    String hint = "";
                                    tentativi--;
                                    
                                    for(int i= 0; i<guessedWord.length(); i++){
                                        if(guessedWord.charAt(i) == currentWord.charAt(i)){
                                            //posizione giusta
                                            hint+= guessedWord.charAt(i) +"+, ";
                                            attempt+= "+, ";
                                        }
                                        else{
                                            if(currentWord.indexOf(guessedWord.charAt(i)) != -1){      //posizione errata ma presente
                                                hint+= guessedWord.charAt(i)+"?, ";
                                                attempt+= "?, ";
                                            }else{                                                      //lettera non presente
                                                hint+= guessedWord.charAt(i)+"X, ";
                                                attempt+= "X, ";
                                            }
                                        }

                                    }
                                    attempt+= "\n";
                                    out.println(hint);
                                    out.println("next");
                                }

                                
                            
                            }        

                        }
                        if(tentativi == 0)      //partita persa
                        utente.setCurrentStreak(0);
                        
                        int tentativiUsati = 12-tentativi;
                        attempt = attempt.replace("TEMP", Integer.toString(tentativiUsati)+"/12");    
                        utente.concatAttempts(attempt);
                    
                        out.println("quit");            //tentativi terminati
                        latestAttempt = "Giocatore: "+utente.getUsername()+"\n";
                        latestAttempt += attempt;

                        break;
                    case "send-statistic":
                        out.println("Partite giocate: "+utente.getNpartite());
                        if(utente.getNpartite() == 0){
                            out.println("Percentuale vittorie: 0%");
                        }else
                            out.println("Percentuale vittorie: "+(utente.getpartiteVinte()*100/utente.getNpartite())+"%");
                        out.println("Ultima streak: "+utente.getCurrentStreak());
                        out.println("Best streak: "+utente.getBestStreak());
                        out.println("Guess distribution: "+utente.getStringDistribuzione());
                        out.println("quit");
                        break;
                    case "share":
                        if(sendPlayerStats() == 0)                              
                            out.println("OK");
                        else
                            out.println("Non hai ancora giocato!");
                        break;
                    case "logout":
                        logout(out);
                        System.out.println("Giocatore "+Thread.currentThread().getId()+" disconnesso");
                        return;
                    default:
                        return;    
                }  
            }
        
        }catch(NoSuchElementException e){
            System.out.println("Giocatore "+Thread.currentThread().getId()+" disconnesso"); 
            if(utente!= null)  utente.online.set(false);
            return; 
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    private int login(Scanner in, PrintWriter out){
       out.println("Inserisci username");
       out.println("Input-request");
        String username = in.nextLine();
        while(!this.loginTable.containsKey(username)){                     //controllo validità username
            out.println("Username non presente");
            out.println("Input-request");
            username = in.nextLine();
        }
        out.println("Inserisci la password");
        out.println("Input-request");

        String veraPassword = this.loginTable.get(username).getPassword();
        String pass = in.nextLine();
        while(!Objects.equals(veraPassword, pass)){
            out.println("Password errata");
            out.println("Input-request");
            pass = in.nextLine();
        }

        if(this.loginTable.get(username).online.compareAndSet(false, true)){        //controllo se l'utente è già online
            out.println("OK");
        }else{
            out.println("Utente già connesso");
            return -1;
        }
        utente = this.loginTable.get(username);
        return 0;                               //successo
    }

    public void logout(PrintWriter out){
        utente.online.set(false);
        

        out.println("OK");
    }
    public int sendPlayerStats(){                //invia l'ultimo tentativo dell'utente a tutti gli utenti connessi
        if(latestAttempt == null)   return -1;
        try{DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(udpAddress);
        byte[] msg = latestAttempt.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, udpPort);
        socket.send(packet);
        System.out.println("Ho inviato");
        socket.close();
        return 0;}
        catch(Exception e){
            System.out.println("errore connessione udp: "+e.getMessage());
            return -1;
        }
    }
    
}
