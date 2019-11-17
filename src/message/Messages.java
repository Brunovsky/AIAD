package message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import types.Repair;
import utils.MalfunctionType;

public class Messages {
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
