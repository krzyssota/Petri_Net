package multiplicator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.*;

public class Main {

    private static class Process implements Runnable {
        String name;
        PetriNet<String> petriNet;
        Collection<Transition<String>> transitions;
        int transitionsFired;

        public Process(String name, PetriNet<String> petriNet, Collection<Transition<String>> transitions) {
            this.name = name;
            this.petriNet = petriNet;
            this.transitions = transitions;
            transitionsFired = 0;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    petriNet.fire(transitions);
                    ++transitionsFired;
                } catch (InterruptedException e) {
                    System.out.println(transitionsFired);
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int m = scanner.nextInt();
        scanner.close();

        if(n > m){
            int tmp = m;
            m = n;
            n = tmp;
        }

        Map<String, Integer> places = new HashMap<>();
        places.put("N", n);
        places.put("M", m);
        places.put("InnerLeft", 1);
        PetriNet<String> net = new PetriNet<>(places, false);

        Map<String, Integer> input;
        Collection<String> reset;
        Collection<String> inhibitor;
        Map<String, Integer> output;

        // distinguished transition
        inhibitor = new HashSet<>();
        inhibitor.add("N");
        inhibitor.add("M");
        inhibitor.add("InnerLeft");
        inhibitor.add("InnerRight");
        inhibitor.add("Outer");
        Transition<String> distinguished = new Transition<>(new HashMap<>(), new HashSet<>(), inhibitor, new HashMap<>());

        // resetting all transition
        reset = new HashSet<>();
        reset.add("InnerLeft");
        reset.add("InnerRight");
        reset.add("Outer");
        inhibitor = new HashSet<>();
        inhibitor.add("N");
        inhibitor.add("M");
        Transition<String> resetingAll = new Transition<>(new HashMap<>(), reset, inhibitor, new HashMap<>());

        // transition between inner left place and M place
        input = Collections.singletonMap("InnerLeft", 1);
        inhibitor = Collections.singleton("M");
        Transition<String> innerWithM = new Transition<>(input, new HashSet<>(), inhibitor, new HashMap<>());

        // inner up transition
        input = Collections.singletonMap("InnerRight", 1);
        inhibitor = Collections.singleton("N");
        output = Collections.singletonMap("InnerLeft", 1);
        Transition<String> innerUp = new Transition<>(input, new HashSet<>(), inhibitor, output);

        // inner down transition
        input = new HashMap<>();
        input.put("InnerLeft", 1);
        input.put("M", 1);
        inhibitor = Collections.singleton("Outer");
        output = Collections.singletonMap("InnerRight", 1);
        Transition<String> innerDown = new Transition<>(input, new HashSet<>(), inhibitor, output);

        // outer left transition
        input = new HashMap<>();
        input.put("Outer", 1);
        input.put("InnerLeft", 1);
        output = new HashMap<>();
        output.put("N", 1);
        output.put("InnerLeft", 1);
        Transition<String> outerLeft = new Transition<>(input, new HashSet<>(), new HashSet<>(), output);

        // outer right transition
        input = new HashMap<>();
        input.put("N", 1);
        input.put("InnerRight", 1);
        output = new HashMap<>();
        output.put("Outer", 1);
        output.put("InnerRight", 1);
        output.put("Result", 1);
        Transition<String> outerRight = new Transition<>(input, new HashSet<>(), new HashSet<>(), output);

        Collection<Transition<String>> transitions = new ArrayList<>();
        transitions.add(resetingAll);
        transitions.add(innerWithM);
        transitions.add(innerUp);
        transitions.add(innerDown);
        transitions.add(outerLeft);
        transitions.add(outerRight);

        Thread A = new Thread(new Process("A", net, transitions));
        Thread B = new Thread(new Process("B", net, transitions));
        Thread C = new Thread(new Process("C", net, transitions));
        Thread D = new Thread(new Process("D", net, transitions));
        A.start();
        B.start();
        C.start();
        D.start();

        Collection<Transition<String>> singleton = Collections.singleton(distinguished);
        try {
            net.fire(singleton);
        } catch (InterruptedException e){
            System.err.println(e.getMessage());
        }
        System.out.println(net.getPlaces().getOrDefault("Result", 0));
        A.interrupt();
        B.interrupt();
        C.interrupt();
        D.interrupt();
    }
}
