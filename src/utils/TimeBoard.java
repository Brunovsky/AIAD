package utils;

import utils.constants.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TimeBoard {

    Map<Date,ArrayList<TimeSlot>> timeBoard;

    public TimeBoard() {
        timeBoard = new HashMap<>();
    }

    public TimeSlot getAvailableSlot(MalfunctionType type) {
        int duration = Constants.getMalfunctionDuration(type);

        // check in timeBoard available time
        // add a timeslot (reserva) to timeboard in the first available time
        // return the timeslot


        return null;
    }

    /**
     * getAvailableSlots()
     * 
     * Recebe o tipo de avaria (-> duração) e 
     * reserva na timeBoard o timeSlot
     * retorna o primeiro timeSlot livre a partir do "agora" do sistema.
     */

}