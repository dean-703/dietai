package com.courtney.dietai.analysis;

import com.courtney.dietai.model.DietEntry;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class NutritionAnalyzer {

    public static DietSummary summarize(List<DietEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("No entries to summarize.");
        }

        List<DietEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(DietEntry::getDate))
                .toList();

        LocalDate start = sorted.get(0).getDate();
        LocalDate end = sorted.get(sorted.size() - 1).getDate();

        Map<LocalDate, List<DietEntry>> byDay = sorted.stream()
                .collect(Collectors.groupingBy(DietEntry::getDate, TreeMap::new, Collectors.toList()));

        Map<LocalDate, Integer> dailyCalories = new LinkedHashMap<>();
        double totalCals = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0, totalFiber = 0, totalSodium = 0;

        for (Map.Entry<LocalDate, List<DietEntry>> day : byDay.entrySet()) {
            double dayCals = 0;
            for (DietEntry e : day.getValue()) {
                totalCals += e.getCalories();
                totalProtein += e.getProtein();
                totalCarbs += e.getCarbs();
                totalFat += e.getFat();
                totalFiber += e.getFiber();
                totalSodium += e.getSodiumMg();

                dayCals += e.getCalories();
            }
            dailyCalories.put(day.getKey(), (int) Math.round(dayCals));
        }

        int daysCount = byDay.size();
        double avgCals = daysCount > 0 ? totalCals / daysCount : 0;
        double avgProtein = daysCount > 0 ? totalProtein / daysCount : 0;
        double avgCarbs = daysCount > 0 ? totalCarbs / daysCount : 0;
        double avgFat = daysCount > 0 ? totalFat / daysCount : 0;
        double avgFiber = daysCount > 0 ? totalFiber / daysCount : 0;
        double avgSodium = daysCount > 0 ? totalSodium / daysCount : 0;

        double proteinCal = totalProtein * 4.0;
        double carbsCal = totalCarbs * 4.0;
        double fatCal = totalFat * 9.0;
        double macroTotal = Math.max(1.0, proteinCal + carbsCal + fatCal);
        double pctProtein = 100.0 * proteinCal / macroTotal;
        double pctCarbs = 100.0 * carbsCal / macroTotal;
        double pctFat = 100.0 * fatCal / macroTotal;

        DietSummary summary = new DietSummary();
        summary.setStartDate(start);
        summary.setEndDate(end);
        summary.setEntriesCount(sorted.size());
        summary.setDaysCount(daysCount);
        summary.setTotalCalories(totalCals);
        summary.setAvgCaloriesPerDay(avgCals);
        summary.setTotalProteinG(totalProtein);
        summary.setTotalCarbsG(totalCarbs);
        summary.setTotalFatG(totalFat);
        summary.setAvgProteinPerDayG(avgProtein);
        summary.setAvgCarbsPerDayG(avgCarbs);
        summary.setAvgFatPerDayG(avgFat);
        summary.setAvgFiberPerDayG(avgFiber);
        summary.setAvgSodiumPerDayMg(avgSodium);
        summary.setMacroPctProtein(pctProtein);
        summary.setMacroPctCarbs(pctCarbs);
        summary.setMacroPctFat(pctFat);
        summary.setDailyCalories(dailyCalories);

        return summary;
    }
}