package com.courtney.dietai.profile;

public class GoalSettings {
    public enum GoalMode { LOSE, MAINTAIN, GAIN }

    private GoalMode mode = GoalMode.MAINTAIN;
    private double weeklyRateKg = 0.0; // kg/week; for maintain this is ignored
    private double proteinPerKg = 1.6; // g per kg bodyweight
    private double fatPerKg = 0.8;     // g per kg bodyweight
    private double fiberTargetG = -1;  // <=0 means auto by calories
    private double sodiumTargetMg = -1; // <=0 means default

    public GoalMode getMode() { return mode; }
    public void setMode(GoalMode mode) { this.mode = mode; }

    public double getWeeklyRateKg() { return weeklyRateKg; }
    public void setWeeklyRateKg(double weeklyRateKg) { this.weeklyRateKg = weeklyRateKg; }

    public double getProteinPerKg() { return proteinPerKg; }
    public void setProteinPerKg(double proteinPerKg) { this.proteinPerKg = proteinPerKg; }

    public double getFatPerKg() { return fatPerKg; }
    public void setFatPerKg(double fatPerKg) { this.fatPerKg = fatPerKg; }

    public double getFiberTargetG() { return fiberTargetG; }
    public void setFiberTargetG(double fiberTargetG) { this.fiberTargetG = fiberTargetG; }

    public double getSodiumTargetMg() { return sodiumTargetMg; }
    public void setSodiumTargetMg(double sodiumTargetMg) { this.sodiumTargetMg = sodiumTargetMg; }
}