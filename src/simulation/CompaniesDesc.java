package simulation;

import strategies.CompanyStrategy;
import strategies.EasyCommonCompanyStrategy;
import strategies.HardSpecializedCompanyStrategy;

public class CompaniesDesc {
    public final int numberCompanies;
    public final Strategy strategy;
    public final int numberTechnicians;

    public enum Strategy {
        PREFER_EASY,
        PREFER_HARD;

        public CompanyStrategy make() {
            switch (this) {
            case PREFER_EASY:
                return new EasyCommonCompanyStrategy();
            case PREFER_HARD:
                return new HardSpecializedCompanyStrategy();
            }
            return null;
        }
    }

    public CompaniesDesc(int number, Strategy strategy, int numTechnicians) {
        this.numberCompanies = number;
        this.strategy = strategy;
        this.numberTechnicians = numTechnicians;
    }
}
