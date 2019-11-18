package simulation;

import strategies.CompanyStrategy;
import strategies.PreferEasyCompanyStrategy;
import strategies.PreferHardCompanyStrategy;

public class CompaniesDesc {
    public int number;
    public Strategy strategy;
    public int numberTechnicians;

    public enum Strategy {
        PREFER_EASY,
        PREFER_HARD;

        public CompanyStrategy make() {
            switch (this) {
            case PREFER_EASY:
                return new PreferEasyCompanyStrategy();
            case PREFER_HARD:
                return new PreferHardCompanyStrategy();
            }
            return null;
        }
    }

    public CompaniesDesc(int number, Strategy strategy, int numberTechnicians) {
        this.number = number;
        this.strategy = strategy;
        this.numberTechnicians = numberTechnicians;
    }
}
