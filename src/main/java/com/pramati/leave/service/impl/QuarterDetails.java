package com.pramati.leave.service.impl;

/**
 * Created with IntelliJ IDEA.
 * User: pramati
 * Date: 17/06/13
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuarterDetails {
    private String months;
    private int applied;
    private int approved;
    private int carryForword;
    private int availed;
    private int accrued;
    private int balance;
    private int canceled;

    public QuarterDetails(String months, int applied, int approved, int carryForword, int availed, int balance, int accrued, int canceled) {
        this.months = months;
        this.applied = applied;
        this.approved = approved;
        this.carryForword = carryForword;
        this.availed = availed;
        this.balance = balance;
        this.accrued = accrued;
        this.canceled = canceled;
    }

    public QuarterDetails() {

    }

    public String getMonths() {
        return months;
    }

    public void setMonths(String months) {
        this.months = months;
    }

    public int getApplied() {
        return applied;
    }

    public void setApplied(int applied) {
        this.applied = applied;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public int getCarryForword() {
        return carryForword;
    }

    public void setCarryForword(int utilized) {
        this.carryForword = utilized;
    }

    public int getAvailed() {
        return availed;
    }

    public void setAvailed(int availed) {
        this.availed = availed;
    }

    public int getAccrued() {
        return accrued;
    }

    public void setAccrued(int accrued) {
        this.accrued = accrued;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getCanceled() {
        return canceled;
    }

    public void setCanceled(int canceled) {
        this.canceled = canceled;
    }
}
