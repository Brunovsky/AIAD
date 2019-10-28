package utils;

public class FinancialAccount {
    private int balance;
    private int loss;
    private int gains;

    public FinancialAccount() {
        this.balance = 0;
        this.loss = 0;
        this.gains = 0;
    }

    public int getBalance() {
        return balance;
    }

    public int getLoss() {
        return loss;
    }

    public int getGains() {
        return gains;
    }

    public void depositMoney(int money) {
        gains += money;
        balance += money;
    }

    public void withdrawMoney(int money) {
        loss += money;
        balance -= money;
    }
}
