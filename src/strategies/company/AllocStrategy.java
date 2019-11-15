package strategies.company;

import java.util.ArrayList;
import java.util.Map;

import agents.Technician;

    /**
     * Strategy that will be used by Leader Technician
     *  to alloc other Technicians and decide prices in the same Station
     */
public interface AllocStrategy {

    /**
     * 
     * Provavelmente recebe:
     * - lista de pedidos (ArrayList)
     * - lista de técnicos  (ArrayList)
     * @return Map com os Pedidos para cada Técnico e preços
     */
    public Map<ArrayList<Technician>, ArrayList< String /* RequestsSlots? */>> decideAllocation();
            // TODO: Allocation function with x Requests per Technician

 }
