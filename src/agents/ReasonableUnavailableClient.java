package agents;

import message.TechnicianMessage;

public class ReasonableUnavailableClient extends Client {

    @Override
    public boolean compareTechnicianMessages(TechnicianMessage msg1, TechnicianMessage msg2) {
        // TODO: return true if msg1 it's better than msg2
        return true;
    }
}
