package utils;

import java.util.ArrayList;

public class TimeBoard {

    ArrayList<RepairSlot> timeBoard;

    public TimeBoard() {
        timeBoard = new ArrayList<>();
    }

    public double getNextAvailableSlotStartTime(double clientRequestSendTime){
        return Math.max(this.timeBoard.get(this.timeBoard.size()-1).getEndSlotTime(), clientRequestSendTime);
    }

    public void addRepairSlot(RepairSlot slot){
        this.timeBoard.add(slot);
    }


}