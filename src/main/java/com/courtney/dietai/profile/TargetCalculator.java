package com.courtney.dietai.profile;

public class TargetCalculator {

    // 1 kg ~ 7700 kcal
    private static final double KCAL_PER_KG = 7700.0;

    public static Targets calculate(UserProfile p, GoalSettings g) {
        Targets t = new Targets();

        // Mifflin-St Jeor
        double bmr = (10 * p.getWeightKg()) + (6.25 * p.getHeightCm()) - (5 * p.getAge()) + (p.getSex() == UserProfile.Sex.MALE ? 5 : -161);
        double tdee = bmr * p.getActivityLevel().factor();

        double dailyAdj = 0;
        if (g.getMode() == GoalSettings.GoalMode.LOSE) dailyAdj = -(g.getWeeklyRateKg() * KCAL_PER_KG / 7.0);
        else if (g.getMode() == GoalSettings.GoalMode.GAIN) dailyAdj = +(g.getWeeklyRateKg() * KCAL_PER_KG / 7.0);

        double calorieTarget = Math.max(1200, tdee + dailyAdj); // basic floor for safety

        double proteinG = Math.max(0, g.getProteinPerKg() * p.getWeightKg());
        double fatG = Math.max(0, g.getFatPerKg() * p.getWeightKg());
        double proteinCal = proteinG * 4.0;
        double fatCal = fatG * 9.0;
        double remainingCal = Math.max(0, calorieTarget - (proteinCal + fatCal));
        double carbsG = remainingCal / 4.0;

        double fiberTarget = g.getFiberTargetG() > 0 ? g.getFiberTargetG() : Math.round((calorieTarget / 1000.0) * 14.0);
        double sodiumTarget = g.getSodiumTargetMg() > 0 ? g.getSodiumTargetMg() : 2300.0;

        t.setBmr(bmr);
        t.setTdee(tdee);
        t.setCalorieTarget(calorieTarget);
        t.setProteinTargetG(proteinG);
        t.setFatTargetG(fatG);
        t.setCarbsTargetG(carbsG);
        t.setFiberTargetG(fiberTarget);
        t.setSodiumTargetMg(sodiumTarget);
        return t;
    }
}