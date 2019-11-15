package strategies.company;

    /**
     * Strategy that will be used by the Company to pay de Technicians
     * and maybe alloc them in the different Stations
     */
public interface PaymentStrategy {

    /**
     * Company pays his technicians
     */
    public void pay();

 }
