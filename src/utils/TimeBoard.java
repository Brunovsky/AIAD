package utils;

import java.util.ArrayList;

public class TimeBoard {
    ArrayList<RepairSlot> timeBoard;

    public TimeBoard() {
        timeBoard = new ArrayList<>();
    }

    public double getNextAvailableSlotStartTime(double clientRequestSendTime) {
        if (timeBoard.isEmpty()) return clientRequestSendTime;
        return Math.max(timeBoard.get(timeBoard.size() - 1).getEndSlotTime(),
                        clientRequestSendTime);
    }

    public void addRepairSlot(RepairSlot slot) {
        timeBoard.add(slot);
    }
}
