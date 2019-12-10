package types;

import utils.MalfunctionType;

public class TypedFinance {
    public Finance easy = new Finance();
    public Finance medium = new Finance();
    public Finance hard = new Finance();
    public int wastedWeight = 0;

    public TypedFinance() {}

    private void add(Workday workday, MalfunctionType type) {
        Finance finance = get(type);
        finance.revenue += workday.assignment.revenue(type);
        finance.proposalWeight += workday.proposal.weight(type);
        finance.assignedWeight += workday.assignment.weight(type);
    }

    public void add(Workday workday) {
        add(workday, MalfunctionType.EASY);
        add(workday, MalfunctionType.MEDIUM);
        add(workday, MalfunctionType.HARD);
        wastedWeight += workday.wastedWeight();
    }

    public void add(TypedFinance finance) {
        easy.add(finance.easy);
        medium.add(finance.medium);
        hard.add(finance.hard);
        wastedWeight += finance.wastedWeight;
    }

    public Finance get(MalfunctionType type) {
        switch (type) {
        case EASY:
            return easy;
        case MEDIUM:
            return medium;
        case HARD:
        default:
            return hard;
        }
    }
}
