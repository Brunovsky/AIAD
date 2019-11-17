package message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import types.Repair;
import utils.MalfunctionType;

public class Messages {
    public static String getClientRequestMessage(HashMap<Integer, Repair> newRepairs,
                                                 HashMap<Integer, Double> adjustments) {
        // request -> id;repairStartTime;malfunctionType;maxprice
        // adjustment -> id;newMaxPrice
        // Message -> request1&request2&:adjustment1&adjustment2&
        String message = "";
        Iterator it = newRepairs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Repair request = (Repair) pair.getValue();
            message += pair.getKey() + ";" + request.getMalfunctionType().getValue() + ";"
                       + request.getPrice() + "&";
        }

        message += ":";

        it = adjustments.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            message += pair.getKey() + ";" + pair.getValue() + "&";
        }

        return message;
    }

    public static HashMap<Integer, Repair> parseClientRequestMessage(String messageContent) {
        HashMap<Integer, Repair> parsedRequests = new HashMap<>();
        String[] splitMessage = messageContent.split(":");
        if (splitMessage.length < 1) {
            return parsedRequests;
        }
        String requestsMessage = splitMessage[0];
        String[] requests = requestsMessage.split("&");

        for (String request : requests) {
            if (!request.equals("")) {
                String[] args = request.split(";");
                Repair clientRequest = new Repair(MalfunctionType.make(Integer.parseInt(args[1])),
                                                  Double.parseDouble(args[2]));
                parsedRequests.put(Integer.parseInt(args[0]), clientRequest);
            }
        }

        return parsedRequests;
    }

    public static HashMap<Integer, Double> parseClientAdjustmentMessage(String messageContent) {
        HashMap<Integer, Double> parsedAdjustments = new HashMap<>();
        String[] splitMessage = messageContent.split(":");

        if (splitMessage.length != 2) {
            return parsedAdjustments;
        }
        String newRequestsMessage = splitMessage[1];
        String[] adjustments = newRequestsMessage.split("&");

        for (String adjustment : adjustments) {
            if (!adjustment.equals("")) {
                String[] args = adjustment.split(";");
                parsedAdjustments.put(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
            }
        }

        return parsedAdjustments;
    }

    public static String getClientResponseMessage(HashMap<Integer, Repair> clientsRepairs) {
        // response ->   id;startTime;malfunctionType;duration;price;assignedTechnician& ...
        // nextResponse
        String message = "";
        Iterator it = clientsRepairs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Repair repair = (Repair) pair.getValue();
            message += pair.getKey() + ";" + repair.getMalfunctionType().getValue() + ";"
                       + repair.getPrice() + "&";
        }
        return message;
    }

    public static HashMap<Integer, Repair> parseClientResponseMessage(String messageContent) {
        HashMap<Integer, Repair> parsedRepairs = new HashMap<>();
        String[] repairs = messageContent.split("&");

        for (String repair : repairs) {
            if (!repair.equals("")) {
                String[] args = repair.split(";");
                Repair r = new Repair(MalfunctionType.make(Integer.parseInt(args[1])),
                                      Double.parseDouble(args[2]));
                parsedRepairs.put(Integer.parseInt(args[0]), r);
            }
        }

        return parsedRepairs;
    }
}
