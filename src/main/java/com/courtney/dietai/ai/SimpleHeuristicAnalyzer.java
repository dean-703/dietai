package com.courtney.dietai.ai;

import com.courtney.dietai.analysis.DietSummary;
import com.courtney.dietai.profile.Targets;
import com.courtney.dietai.profile.UserProfile;
import java.util.Locale;

public class SimpleHeuristicAnalyzer {

    public String analyze(DietSummary s, UserProfile p, Targets t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Offline analysis (no API key). Personalized insights:\n\n");

        sb.append(String.format(Locale.US,
                "- Profile: %s, %d y, %.1f kg, %.0f cm, activity %s\n",
                p.getSex(), p.getAge(), p.getWeightKg(), p.getHeightCm(), p.getActivityLevel()));
        sb.append(String.format(Locale.US,
                "- Targets: Calories %.0f, Protein %.0fg, Carbs %.0fg, Fat %.0fg, Fiber %.0fg, Sodium %.0fmg\n",
                t.getCalorieTarget(), t.getProteinTargetG(), t.getCarbsTargetG(), t.getFatTargetG(), t.getFiberTargetG(), t.getSodiumTargetMg()));

        sb.append(String.format(Locale.US,
                "- Actual averages: Calories %.0f (%+.0f vs target), Protein %.0fg (%+.0fg), Carbs %.0fg (%+.0fg), Fat %.0fg (%+.0fg)\n",
                s.getAvgCaloriesPerDay(), s.getAvgCaloriesPerDay() - t.getCalorieTarget(),
                s.getAvgProteinPerDayG(), s.getAvgProteinPerDayG() - t.getProteinTargetG(),
                s.getAvgCarbsPerDayG(), s.getAvgCarbsPerDayG() - t.getCarbsTargetG(),
                s.getAvgFatPerDayG(), s.getAvgFatPerDayG() - t.getFatTargetG()));

        sb.append(String.format(Locale.US,
                "- Macro balance (%% calories): Protein %.0f%%, Carbs %.0f%%, Fat %.0f%%\n",
                s.getMacroPctProtein(), s.getMacroPctCarbs(), s.getMacroPctFat()));

        // Practical heuristics
        if (s.getAvgProteinPerDayG() < t.getProteinTargetG() * 0.9) {
            sb.append("  • Protein appears below target; include a lean protein source each meal (eggs, Greek yogurt, tofu, fish, legumes).\n");
        }
        if (s.getAvgFatPerDayG() > t.getFatTargetG() * 1.2) {
            sb.append("  • Fat intake is above target; check portions of oils, nuts, cheese, and fried foods.\n");
        }
        if (s.getAvgCarbsPerDayG() > t.getCarbsTargetG() * 1.2) {
            sb.append("  • Carbs exceed target; swap refined grains for whole grains and add non-starchy veggies.\n");
        }
        if (s.getAvgFiberPerDayG() < t.getFiberTargetG() * 0.8) {
            sb.append("  • Fiber is low; add beans/lentils, whole grains, fruits, and extra vegetables.\n");
        }
        if (s.getAvgSodiumPerDayMg() > t.getSodiumTargetMg() * 1.2) {
            sb.append("  • Sodium is high; favor fresh foods, choose low-sodium options, and season with herbs/spices.\n");
        }

        sb.append("\nAction plan:\n");
        sb.append("• Pre-plan 1–2 high-protein snacks to close protein gaps.\n");
        sb.append("• Fill half the plate with vegetables at lunch/dinner to lift fiber and reduce calories.\n");
        sb.append("• Swap sugary drinks for water or unsweetened tea/coffee.\n");
        sb.append("• Cook with measured amounts of oil to manage fat intake.\n");
        sb.append("• Log consistently for another 1–2 weeks to gauge trends.\n");

        return sb.toString();
    }
}