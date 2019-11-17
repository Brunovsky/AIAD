package simulation;

import strategies.company.CompanyStrategy;

public class CompaniesDesc {
    private int number;
    private CompanyStrategy strategy;
    private int numberTechnicians;

    public CompaniesDesc(int number, CompanyStrategy strategy, int numberTechnicians) {
        this.number = number;
        this.strategy = strategy;
        this.numberTechnicians = numberTechnicians;
    }

    public int getNumber() {
        return number;
    }

    public CompanyStrategy getStrategy() {
        return strategy;
    }

    public int getNumberTechnicians() {
        return numberTechnicians;
    }
}
