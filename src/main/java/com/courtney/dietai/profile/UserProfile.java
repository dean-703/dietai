
package com.courtney.dietai.profile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserProfile {
    public enum Sex { MALE, FEMALE }
    public enum ActivityLevel {
        SEDENTARY(1.2), LIGHTLY_ACTIVE(1.375), MODERATELY_ACTIVE(1.55), VERY_ACTIVE(1.725), ATHLETE(1.9);
        private final double factor;
        ActivityLevel(double factor) { this.factor = factor; }
        public double factor() { return factor; }
    }

    private int age = 30;
    private Sex sex = Sex.FEMALE;
    private double weightKg = 70;
    private double heightCm = 170;
    private ActivityLevel activityLevel = ActivityLevel.SEDENTARY;

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public Sex getSex() { return sex; }
    public void setSex(Sex sex) { this.sex = sex; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    @JsonProperty("activityLevel")
    public ActivityLevel getActivityLevel() { return activityLevel; }
    public void setActivityLevel(ActivityLevel activityLevel) { this.activityLevel = activityLevel; }
}