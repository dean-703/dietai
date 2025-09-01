package com.courtney.dietai.model;

import java.time.LocalDate;

public class DietEntry {
    private LocalDate date;
    private String meal;
    private String item;
    private String quantityOrDuration;

    private double calories;
    private double carbs;
    private double protein;
    private double fat;

    private double sodiumMg;
    private double fiber;

    private String notes;

    public DietEntry() {}

    public DietEntry(LocalDate date, String meal, String item, String quantityOrDuration,
                     double calories, double carbs, double protein, double fat,
                     double sodiumMg, double fiber, String notes) {
        this.date = date;
        this.meal = meal;
        this.item = item;
        this.quantityOrDuration = quantityOrDuration;
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.sodiumMg = sodiumMg;
        this.fiber = fiber;
        this.notes = notes;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMeal() { return meal; }
    public void setMeal(String meal) { this.meal = meal; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getQuantityOrDuration() { return quantityOrDuration; }
    public void setQuantityOrDuration(String quantityOrDuration) { this.quantityOrDuration = quantityOrDuration; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public double getSodiumMg() { return sodiumMg; }
    public void setSodiumMg(double sodiumMg) { this.sodiumMg = sodiumMg; }

    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}