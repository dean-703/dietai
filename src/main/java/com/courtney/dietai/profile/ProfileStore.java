package com.courtney.dietai.profile;

import java.util.prefs.Preferences;

public class ProfileStore {
    private final Preferences prefs = Preferences.userRoot().node("com.courtney.dietai.profile");

    public UserProfile loadProfile() {
        UserProfile p = new UserProfile();
        p.setAge(prefs.getInt("age", p.getAge()));
        p.setSex(UserProfile.Sex.valueOf(prefs.get("sex", p.getSex().name())));
        p.setWeightKg(prefs.getDouble("weightKg", p.getWeightKg()));
        p.setHeightCm(prefs.getDouble("heightCm", p.getHeightCm()));
        p.setActivityLevel(UserProfile.ActivityLevel.valueOf(prefs.get("activity", p.getActivityLevel().name())));
        return p;
    }

    public void saveProfile(UserProfile p) {
        prefs.putInt("age", p.getAge());
        prefs.put("sex", p.getSex().name());
        prefs.putDouble("weightKg", p.getWeightKg());
        prefs.putDouble("heightCm", p.getHeightCm());
        prefs.put("activity", p.getActivityLevel().name());
    }

    public GoalSettings loadGoals() {
        GoalSettings g = new GoalSettings();
        g.setMode(GoalSettings.GoalMode.valueOf(prefs.get("mode", g.getMode().name())));
        g.setWeeklyRateKg(prefs.getDouble("weeklyRateKg", g.getWeeklyRateKg()));
        g.setProteinPerKg(prefs.getDouble("proteinPerKg", g.getProteinPerKg()));
        g.setFatPerKg(prefs.getDouble("fatPerKg", g.getFatPerKg()));
        g.setFiberTargetG(prefs.getDouble("fiberTargetG", g.getFiberTargetG()));
        g.setSodiumTargetMg(prefs.getDouble("sodiumTargetMg", g.getSodiumTargetMg()));
        return g;
    }

    public void saveGoals(GoalSettings g) {
        prefs.put("mode", g.getMode().name());
        prefs.putDouble("weeklyRateKg", g.getWeeklyRateKg());
        prefs.putDouble("proteinPerKg", g.getProteinPerKg());
        prefs.putDouble("fatPerKg", g.getFatPerKg());
        prefs.putDouble("fiberTargetG", g.getFiberTargetG());
        prefs.putDouble("sodiumTargetMg", g.getSodiumTargetMg());
    }
}