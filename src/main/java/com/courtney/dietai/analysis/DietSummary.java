package com.courtney.dietai.analysis;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class DietSummary {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private int entriesCount;
    private int daysCount;

    private double totalCalories;
    private double avgCaloriesPerDay;

    private double totalProteinG;
    private double totalCarbsG;
    private double totalFatG;

    private double avgProteinPerDayG;
    private double avgCarbsPerDayG;
    private double avgFatPerDayG;

    private double avgFiberPerDayG;
    private double avgSodiumPerDayMg;

    private double macroPctProtein;
    private double macroPctCarbs;
    private double macroPctFat;

    private Map<LocalDate, Integer> dailyCalories = new LinkedHashMap<>();

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getEntriesCount() { return entriesCount; }
    public void setEntriesCount(int entriesCount) { this.entriesCount = entriesCount; }

    public int getDaysCount() { return daysCount; }
    public void setDaysCount(int daysCount) { this.daysCount = daysCount; }

    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }

    public double getAvgCaloriesPerDay() { return avgCaloriesPerDay; }
    public void setAvgCaloriesPerDay(double avgCaloriesPerDay) { this.avgCaloriesPerDay = avgCaloriesPerDay; }

    public double getTotalProteinG() { return totalProteinG; }
    public void setTotalProteinG(double totalProteinG) { this.totalProteinG = totalProteinG; }

    public double getTotalCarbsG() { return totalCarbsG; }
    public void setTotalCarbsG(double totalCarbsG) { this.totalCarbsG = totalCarbsG; }

    public double getTotalFatG() { return totalFatG; }
    public void setTotalFatG(double totalFatG) { this.totalFatG = totalFatG; }

    public double getAvgProteinPerDayG() { return avgProteinPerDayG; }
    public void setAvgProteinPerDayG(double avgProteinPerDayG) { this.avgProteinPerDayG = avgProteinPerDayG; }

    public double getAvgCarbsPerDayG() { return avgCarbsPerDayG; }
    public void setAvgCarbsPerDayG(double avgCarbsPerDayG) { this.avgCarbsPerDayG = avgCarbsPerDayG; }

    public double getAvgFatPerDayG() { return avgFatPerDayG; }
    public void setAvgFatPerDayG(double avgFatPerDayG) { this.avgFatPerDayG = avgFatPerDayG; }

    public double getAvgFiberPerDayG() { return avgFiberPerDayG; }
    public void setAvgFiberPerDayG(double avgFiberPerDayG) { this.avgFiberPerDayG = avgFiberPerDayG; }

    public double getAvgSodiumPerDayMg() { return avgSodiumPerDayMg; }
    public void setAvgSodiumPerDayMg(double avgSodiumPerDayMg) { this.avgSodiumPerDayMg = avgSodiumPerDayMg; }

    public double getMacroPctProtein() { return macroPctProtein; }
    public void setMacroPctProtein(double macroPctProtein) { this.macroPctProtein = macroPctProtein; }

    public double getMacroPctCarbs() { return macroPctCarbs; }
    public void setMacroPctCarbs(double macroPctCarbs) { this.macroPctCarbs = macroPctCarbs; }

    public double getMacroPctFat() { return macroPctFat; }
    public void setMacroPctFat(double macroPctFat) { this.macroPctFat = macroPctFat; }

    public Map<LocalDate, Integer> getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(Map<LocalDate, Integer> dailyCalories) { this.dailyCalories = dailyCalories; }
}