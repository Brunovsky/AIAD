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

    public int getNumberOfTimeSlots() {
        return timeBoard.size();
    }

    public double getReceipts() {
        double receipt = 0;
        for (RepairSlot repairSlot : timeBoard) {
            receipt += repairSlot.getRepairPrice();
        }
        return receipt;
    }

    public double getLastSlotEndTime(){
        if(timeBoard.size() != 0){
            return timeBoard.get(timeBoard.size()-1).getEndSlotTime();
        } else {
            return 0;
        }
    }

    public double getOccupiedTime(){
        double occupiedTime = 0;
        for (RepairSlot repairSlot : timeBoard) {
            occupiedTime += (repairSlot.getEndSlotTime()-repairSlot.getStartSlotTime());
        }
        return occupiedTime;
    }

    public double getTravelTime(){
        double travelTime = 0;
        for (RepairSlot repairSlot : timeBoard) {
            travelTime += (repairSlot.getStartRepairTime()-repairSlot.getStartSlotTime())*2;
        }
        return travelTime;
    }

    public double getWorkTime(){
        double workTime = 0;
        for (RepairSlot repairSlot : timeBoard) {
            workTime += (repairSlot.getEndRepairTime()-repairSlot.getStartRepairTime());
        }
        return workTime;
    }

    @Override
    public String toString() {
        return "TimeBoard{" +
                "timeBoard=" + timeBoard +
                '}';
    }

    //percentagem de trabalho


}
