package strategies.company;

import java.util.Map;

import jade.core.AID;

/**
 * Strategy that will be used by Company to decide Technicians' allocation in
 * the different Stations
 */
public interface TechDistributionStrategy {

    /**
     * Receives technicians and stations and decide allocation
     * Returns a Map with each Station as key and Technicians allocated
     * @param technicians
     * @param stations
     * @return
     */
    public Map<AID, AID> getAllocTechnicians(Map<AID, String> technicians, Map<AID, String> stations);
            // TODO: Allocation function

 }
