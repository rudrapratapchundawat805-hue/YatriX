package com.transit;

public class UserSession {
    private final String name;
    private final int age;
    private final double height;
    private final char gender;
    private final boolean hasMetroCard;

    public UserSession(String name, int age, double height, char gender, boolean hasMetroCard) {
        this.name = name;
        this.age = age;
        this.height = height;
        this.gender = Character.toUpperCase(gender);
        this.hasMetroCard = hasMetroCard;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getHeight() {
        return height;
    }

    public char getGender() {
        return gender;
    }

    public boolean hasMetroCard() {
        return hasMetroCard;
    }

    @Override
    public String toString() {
        return "User: " + name + " | Age: " + age + " | Height: " + height + "ft | Gender: " + gender +
                " | Metro Card: " + (hasMetroCard ? "Yes" : "No");
    }
}