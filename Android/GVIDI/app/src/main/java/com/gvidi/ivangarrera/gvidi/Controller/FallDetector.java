package com.example.ivangarrera.example.Controller;

import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class FallDetector {
    private static FallDetector fallDetector_instance = null;
    private static Semaphore mutex = new Semaphore(1, true);
    private FD_State my_state;
    private int counter;
    private int total_sum;
    private long timestamp_last_state_free_fall;
    private final int TIME_LAST_FREE_FALL = 3; // Time is in seconds
    private final int TOTAL_COUNTER = 10;
    private HashMap<Pair<FD_State, FD_Event>, FD_State> transition_matrix;

    private FallDetector() {
        my_state = FD_State.FD_NORMAL;
        counter = 0;
        total_sum = 0;
        timestamp_last_state_free_fall = -1;
        transition_matrix = new HashMap<>();

        transition_matrix.put(Pair.create(FD_State.FD_NORMAL, FD_Event.FD_DataInsideThreshold), FD_State.FD_NORMAL);
        transition_matrix.put(Pair.create(FD_State.FD_NORMAL, FD_Event.FD_DataMoreThreshold), FD_State.FD_NORMAL);
        transition_matrix.put(Pair.create(FD_State.FD_NORMAL, FD_Event.FD_DataLessThreshold), FD_State.FD_FREE_FALL);

        transition_matrix.put(Pair.create(FD_State.FD_FREE_FALL, FD_Event.FD_DataInsideThreshold), FD_State.FD_NORMAL);
        transition_matrix.put(Pair.create(FD_State.FD_FREE_FALL, FD_Event.FD_DataLessThreshold), FD_State.FD_FREE_FALL);
        transition_matrix.put(Pair.create(FD_State.FD_FREE_FALL, FD_Event.FD_DataMoreThreshold), FD_State.FD_IMPACT);

        transition_matrix.put(Pair.create(FD_State.FD_IMPACT, FD_Event.FD_MeanIsNot9_8), FD_State.FD_NORMAL);
        transition_matrix.put(Pair.create(FD_State.FD_IMPACT, FD_Event.FD_MeanIs9_8), FD_State.FD_FALL);

        transition_matrix.put(Pair.create(FD_State.FD_FALL, FD_Event.FD_DataInsideThreshold), FD_State.FD_NORMAL);
        transition_matrix.put(Pair.create(FD_State.FD_FALL, FD_Event.FD_DataMoreThreshold), FD_State.FD_NORMAL);
        transition_matrix.put(Pair.create(FD_State.FD_FALL, FD_Event.FD_DataLessThreshold), FD_State.FD_NORMAL);
    }

    public static FallDetector getInstance() {
        try {
            mutex.acquire(1);
            if (fallDetector_instance == null) {
                fallDetector_instance = new FallDetector();
            }
        } catch (Exception ex) {
        } finally {
            mutex.release(1);
            return fallDetector_instance;
        }
    }

    public void makeTransition(FD_Event event, double data) {
        try {
            mutex.acquire();
            if (my_state.equals(FD_State.FD_IMPACT)) {
                // If I am in the IMPACT state, calculate the mean of all the values obtained in the last
                // seconds and see if the force is about G m/s^2. If it is the case, we have gotten a fall
                if (counter == TOTAL_COUNTER) {
                    total_sum /= TOTAL_COUNTER;
                    if (total_sum >= (9.8 - 9.8 * 0.15) && total_sum <= (9.8 + 9.8 * 0.15)) {
                        Log.d("GVIDI", String.valueOf(total_sum));
                        event = FD_Event.FD_MeanIs9_8;
                    } else {
                        Log.d("GVIDI", String.valueOf(total_sum));
                        event = FD_Event.FD_MeanIsNot9_8;
                    }
                    total_sum = 0;
                    counter = 0;
                    my_state = transition_matrix.get(Pair.create(my_state, event));
                } else {
                    counter++;
                    total_sum += data;
                }
            } else if (my_state.equals(FD_State.FD_NORMAL) && event.equals(FD_Event.FD_DataMoreThreshold)) {
                // If I am in the NORMAL state, I receive a DMT event and in the last few time I have been
                // in the FREE_FALL state, I consider that the current NORMAL state is noise and I transition
                // to the IMPACT state
                long elapsed_time = (System.currentTimeMillis() / 1000) - timestamp_last_state_free_fall;
                if (elapsed_time <= TIME_LAST_FREE_FALL) {
                    // Go to IMPACT state
                    my_state = transition_matrix.get(Pair.create(FD_State.FD_FREE_FALL, event));
                }
            } else {
                if (my_state.equals(FD_State.FD_FREE_FALL)) {
                    timestamp_last_state_free_fall = System.currentTimeMillis() / 1000;
                }
                my_state = transition_matrix.get(Pair.create(my_state, event));
            }
        } catch (Exception ex) {

        } finally {
            mutex.release(1);
        }
    }

    public FD_State getMyState() {
        return my_state;
    }
}
