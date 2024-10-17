package Server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WordExtractor implements Runnable{
    private HashSet<String> vocabulary;
    public AtomicReference<String> SecretWord;
    public AtomicInteger idgame;

    public WordExtractor(HashSet<String> vocabulary, AtomicReference<String> SecretWord, AtomicInteger idgame){
        this.vocabulary = vocabulary;
        this.SecretWord = SecretWord;
        this.idgame = idgame;
    }

    public void run(){
        String randomLine = null;
        int randomIndex = new Random().nextInt(vocabulary.size());
        Iterator<String> iterator = vocabulary.iterator();
        for (int i = 0; i < randomIndex; i++) {
            iterator.next();
        }
        randomLine = iterator.next();
        
        SecretWord.set(randomLine);
        idgame.incrementAndGet();
        
        System.out.println("Parola cambiata: "+SecretWord);
        return;
        
    }
}
