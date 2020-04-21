package petrinet;

import java.util.*;

public class Transition<T> {

    private Map<T, Integer> input;
    private Collection<T> reset;
    private Collection<T> inhibitor;
    private Map<T, Integer> output;

    public Transition(Map<T, Integer> input, Collection<T> reset, Collection<T> inhibitor, Map<T, Integer> output) {
        this.input = new HashMap<>(input);
        this.reset = new HashSet<>(reset);
        this.inhibitor = new HashSet<>(inhibitor);
        this.output = new HashMap<>(output);
    }

    static <T> Transition<T> findEnabledTransition(Map<T, Integer> marking, Collection<Transition<T>> transitions){
        for (Transition<T> t : transitions) {
            if (t.isEnabled(marking)) {
                return t;
            }
        }
        return null;
    }

    private Integer getTokens(Map<T, Integer> marking, T place){
        return marking.getOrDefault(place, 0);
    }

    boolean isEnabled(Map<T, Integer> marking){
        for (Map.Entry<T,Integer> inputEntry : input.entrySet()){
            Integer tokensNo = getTokens(marking, inputEntry.getKey());
            if(tokensNo < inputEntry.getValue()) return false;
        }
        for (T inhibitorArc : inhibitor){
            Integer tokensNo = getTokens(marking, inhibitorArc);
            if(tokensNo != 0) return false;
        }
        return true;
    }

    Map<T, Integer> fireTransition(Map<T, Integer> marking){

        for (Map.Entry<T,Integer> inputEntry : input.entrySet()){
            T key = inputEntry.getKey();
            Integer value = inputEntry.getValue();
            Integer currTokensNo = getTokens(marking, key);

            if(currTokensNo - value == 0){
                marking.remove(key);
            } else {
                marking.replace(key, currTokensNo - value);
            }
        }

        for (T resetArc : reset){
            marking.remove(resetArc);
        }

        for (Map.Entry<T,Integer> outputEntry : output.entrySet()){
            T key = outputEntry.getKey();
            Integer value = outputEntry.getValue();
            Integer currTokensNo = getTokens(marking, key);

            if(!marking.containsKey(key)) {
                marking.put(key, value);
            } else {
                marking.replace(key, currTokensNo + value);
            }
        }
        return marking;

    }
}