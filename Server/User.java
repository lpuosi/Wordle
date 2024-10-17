package Server;

import java.util.concurrent.atomic.AtomicBoolean;

public class User{
    private String username;
    private String password;
    private int npartite;
    private int partiteVinte;
    private int bestStreak;
    private int currentStreak;
    public  AtomicBoolean online= new AtomicBoolean(false);
    private int lastGame;
    //distribuzione
    private int[] distribuzione;
    //tentativi per udp
    private String attempts;

    
    public User(String user, String pass){
        this.username = user;
        this.password = pass;
        this.npartite = 0;
        this.partiteVinte = 0;
        this.bestStreak = 0;
        this.currentStreak = 0;
        this.lastGame = -1;
        //this.online = new AtomicBoolean(false);
        distribuzione = new int[12];
        for(int i = 0; i<12; i++){
            distribuzione[i] = 0;
        }                 
        attempts ="";
    }
        //getter e setter
    public String getUsername(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    
    public int getNpartite() {
        return npartite;
    }
    public void setNpartite(int npartite) {
        this.npartite = npartite;
    }
    
    public int getpartiteVinte() {
        return partiteVinte;
    }
    public void setpartiteVinte(int partiteVinte) {
        this.partiteVinte = partiteVinte;
    }
    
    public int getBestStreak() {
        return bestStreak;
    }
    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }
    public int getCurrentStreak() {
        return currentStreak;
    }
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }
    public void setOnline(){                    //per gson
        this.online = new AtomicBoolean(false);
    }
    public AtomicBoolean getOnline(){
        return this.online;
    }
     public int getLastGame() {
        return lastGame;
    }
    public void setLastGame(int lastGame) {
        this.lastGame = lastGame;
    }
    
    public int[] getDistribuzione() {
        return distribuzione;
    }
    public void setDistribuzione(int[] distribuzione) {
        this.distribuzione = distribuzione;
    }
    public int indexofDistribuzione(int indice){
        return this.distribuzione[indice];
    }
    public String getStringDistribuzione(){
        String distr = "";
        for(int i =0; i<distribuzione.length-1; i++){
            distr+= (i+1)+":"+distribuzione[i]+", ";
        }
        distr+= (distribuzione.length)+":"+distribuzione[distribuzione.length-1];
        return distr;
    }
    public void concatAttempts(String s){
        this.attempts+= s;
    }
    public void clearAttempts(){
        this.attempts = "";
    }
    public String getAttempts(){
        return this.attempts;
    }
}
