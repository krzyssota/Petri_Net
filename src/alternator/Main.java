package alternator;

import petrinet.PetriNet;
import petrinet.Transition;
import java.util.*;

public class Main {

    private static class Process implements Runnable {
        String name;
        PetriNet<String> petriNet;
        Collection<Transition<String>> transitions;

        public Process(String name, PetriNet<String> petriNet, Collection<Transition<String>> transitions) {
            this.name = name;
            this.petriNet = petriNet;
            this.transitions = transitions;
        }
        void protokół() throws InterruptedException{
            petriNet.fire(transitions);
        }
        void sekcjaKrytyczna(){
            System.out.print(name);
            System.out.print('.');
        }

        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    protokół();
                    sekcjaKrytyczna();
                    protokół();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {

        Map<String, Integer> places = new HashMap<>();
        places.put("First", 2);
        PetriNet<String> net = new PetriNet<>(places, false);
        Map<String, Integer> input;
        Collection<String> reset;
        Collection<String> inhibitor;
        Map<String, Integer> output;

        input =  Collections.singletonMap("First", 1);
        inhibitor = Collections.singleton("Third");
        output = Collections.singletonMap("Second", 1);
        Transition<String> transitionA = new Transition<>(input, new HashSet<>(), inhibitor, output);
        Thread A = new Thread(new Process("A", net, Collections.singleton(transitionA)));

        input = Collections.singletonMap("Second", 1);
        inhibitor = Collections.singleton("First");
        output = Collections.singletonMap("Third", 1);
        Transition<String> transitionB = new Transition<>(input, new HashSet<>(), inhibitor, output);
        Thread B = new Thread(new Process("B", net, Collections.singleton(transitionB)));

        input = Collections.singletonMap("Third", 1);
        inhibitor = Collections.singleton("Second");
        output = Collections.singletonMap("First", 1);
        Transition<String> transitionC = new Transition<>(input, new HashSet<>(), inhibitor, output);
        Thread C = new Thread(new Process("C", net, Collections.singleton(transitionC)));

        Collection<Transition<String>> transitions = new ArrayList<>();
        transitions.add(transitionA);
        transitions.add(transitionB);
        transitions.add(transitionC);

        Set<Map<String, Integer>> reachableMarkings =  net.reachable(transitions);
        for(Map<String, Integer> marking : reachableMarkings){
            if(!net.checkSecurityCondition(marking)){
                System.err.println("Security condition not met.");
                return;
            }
        }
        System.out.println(reachableMarkings.size());

        A.start();
        B.start();
        C.start();

        try {
            Thread.sleep(30000);
            A.interrupt();
            B.interrupt();
            C.interrupt();
        }catch (InterruptedException e){
            System.err.println("Główny wątek przerwany.");
        }
    }
}
