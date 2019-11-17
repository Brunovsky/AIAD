package simulation;

import strategies.company.CompanyStrategy;

public class CompaniesDesc {
    public int number;
    public CompanyStrategy strategy;
    public int numberTechnicians;

    public CompaniesDesc(int number, CompanyStrategy strategy, int numberTechnicians) {
        this.number = number;
        this.strategy = strategy;
        this.numberTechnicians = numberTechnicians;
    }
}
