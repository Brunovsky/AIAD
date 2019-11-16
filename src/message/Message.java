package message;

import utils.MalfunctionType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Message {

    public static String getClientMalFunctionRequestMessage(HashMap<Integer, ClientRequest> requests, HashMap<Integer, Double> newPriceRequests){
        String message = "";
        Iterator it = requests.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            ClientRequest request = (ClientRequest) pair.getValue();
            message += pair.getKey() + ";" + request.getRepairTime() + ";" + request.getMalfunctionType().getValue() + ";" + request.getMaxPrice() + "&";
        }

        message += ":";

        it = newPriceRequests.entrySet().iterator();
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

}
