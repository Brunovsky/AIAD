package message;

import jade.core.AID;
import types.ClientRequest;
import types.Repair;
import utils.MalfunctionType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Messages {

    public static String getClientRequestMessage(HashMap<Integer, ClientRequest> newRepairs, HashMap<Integer, Double> adjustments){
        // request -> id;repairStartTime;malfunctionType;maxprice
        // adjustment -> id;newMaxPrice
        // Message -> request1&request2&:adjustment1&adjustment2&
        String message = "";
        Iterator it = newRepairs.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            ClientRequest request = (ClientRequest) pair.getValue();
            message += pair.getKey() + ";" + request.getRepairStartTime() + ";" + request.getMalfunctionType().getValue() + ";" + request.getMaxPrice() + "&";
        }

        message += ":";

        it = adjustments.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            message += pair.getKey() + ";" + pair.getValue() + "&";
        }

        return message;
    }

    public static HashMap<Integer, ClientRequest> parseClientRequestMessage(String messageContent){
        HashMap<Integer, ClientRequest> parsedRequests = new HashMap<>();
        String[] splitMessage = messageContent.split(":");
        if(splitMessage.length < 1){
            return parsedRequests;
        }
        String requestsMessage = splitMessage[0];
        String[] requests = requestsMessage.split("&");

        for (String request:requests) {
            if(!request.equals("")){
                String[] args = request.split(";");
                ClientRequest clientRequest = new ClientRequest(Double.parseDouble(args[1]), MalfunctionType.make(Integer.parseInt(args[2])), Double.parseDouble(args[3]));
                parsedRequests.put(Integer.parseInt(args[0]), clientRequest);
            }
        }

        return parsedRequests;
    }

    public static HashMap<Integer, Double> parseClientAdjustmentMessage(String messageContent){
        HashMap<Integer, Double> parsedAdjustments = new HashMap<>();
        String[] splitMessage = messageContent.split(":");

        if(splitMessage.length != 2){
            return parsedAdjustments;
        }
        String newRequestsMessage = splitMessage[1];
        String[] adjustments = newRequestsMessage.split("&");

        for (String adjustment:adjustments){
            if(!adjustment.equals("")){
                String[] args = adjustment.split(";");
                parsedAdjustments.put(Integer.parseInt(args[0]), Double.parseDouble(args[1]));
            }
        }

        return parsedAdjustments;
    }

    public static String getClientResponseMessage(HashMap<Integer, Repair> clientsRepairs){
        // response ->   id;startTime;malfunctionType;duration;price;assignedTechnician& ... nextResponse
        String message = "";
        Iterator it = clientsRepairs.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            Repair repair = (Repair) pair.getValue();
            message += pair.getKey() + ";" + repair.getStartTime() + ";" + repair.getMalfunctionType().getValue() + ";" + repair.getDuration() + ";" + repair.getPrice() + ";" + repair.getAssignedTechnician() + "&";
        }
        return message;
    }

    public static HashMap<Integer, Repair> parseClientResponseMessage(String messageContent){
        HashMap<Integer, Repair> parsedRepairs = new HashMap<>();
        String[] repairs = messageContent.split("&");

        for (String repair:repairs){
            if(!repair.equals("")){
                String[] args = repair.split(";");
                Repair r = new Repair(Double.parseDouble(args[1]), MalfunctionType.make(Integer.parseInt(args[2])), Double.parseDouble(args[3]), Double.parseDouble(args[4]), args[5]);
                parsedRepairs.put(Integer.parseInt(args[0]), r);
            }
        }

        return parsedRepairs;
    }

}
