package petrinet;

import java.util.*;
import java.util.concurrent.Semaphore;
import static petrinet.Transition.findEnabledTransition;

public class PetriNet<T> {

    private HashMap<T, Integer> places;
    public HashMap<T, Integer> getPlaces() { return places; }
    private static Semaphore mutex = new Semaphore(1);
    private List<Map.Entry<Collection<Transition<T>>, Semaphore>> queue;

    public PetriNet(Map<T, Integer> initial, boolean fair) {
        this.places = new HashMap<>(initial);
        this.queue = new LinkedList<>();
    }

    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {

        HashSet<Map<T, Integer>> reachableMarkingsSet = new HashSet<>();
        Stack<Map<T, Integer>> stack = new Stack<>();
        stack.add(places);

        while (!stack.empty()) {

            Map<T, Integer> currentMarking = stack.pop();
            if(!reachableMarkingsSet.contains(currentMarking)) {

                for (Transition<T> t : transitions) {
                    if(t.isEnabled(currentMarking)) {

                        Map<T, Integer> newMarking = t.fireTransition(new HashMap<>(currentMarking));
                        stack.push(newMarking);
                    }
                }
                reachableMarkingsSet.add(currentMarking);
            }

        }
        return reachableMarkingsSet;
    }

    private boolean releaseBlockedThread(){ // finds first (chronologically) thread that has at least one enabled transition
        Iterator<Map.Entry<Collection<Transition<T>>,Semaphore> > it = queue.iterator();
        while(it.hasNext()){
            Map.Entry<Collection<Transition<T>>,Semaphore> threadWaiting = it.next();
            if(findEnabledTransition(places, threadWaiting.getKey()) != null){
                Semaphore s = threadWaiting.getValue();
                it.remove();
                s.release();
                return true;
            }
        }
        return false;
    }


    public Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {

        mutex.acquire();
        Transition<T> enabledTransition = findEnabledTransition(places, transitions);
        boolean otherThreadWasWokenUp = false;

        try {
            if (enabledTransition != null) {
                enabledTransition.fireTransition(places);
                otherThreadWasWokenUp = releaseBlockedThread();
                return enabledTransition;
            } else { // no transitions to fire
                Semaphore ownMutex = new Semaphore(0);
                queue.add(new AbstractMap.SimpleEntry<>(transitions, ownMutex));
                mutex.release();
                ownMutex.acquire();

                enabledTransition = findEnabledTransition(places, transitions);
                assert (enabledTransition != null); // current marking allows at least one transition to be fired
                enabledTransition.fireTransition(places);
                otherThreadWasWokenUp = releaseBlockedThread();
                return enabledTransition;
            }
        } finally {
            if(!otherThreadWasWokenUp) mutex.release(); //release if didn't wake up any waiting thread
        }
    }

    public boolean checkSecurityCondition(Map<T, Integer> marking){
        Integer sum = 0;
        for(Map.Entry<T, Integer> entry : marking.entrySet()){
            sum += entry.getValue();
        }
        return sum == 2;
    }
}
