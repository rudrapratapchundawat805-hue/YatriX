package com.transit;


public interface FareCalculator {
    double calculateFare(int numStations, UserSession user);
    String getFareCalculationBreakdown(int numStations, UserSession user);

    static FareCalculator getCalculator(String mode) {
        switch (mode.toUpperCase()) {
            case "AMTS":  return new AMTSFareCalculator();
            case "METRO": return new MetroFareCalculator();
            case "BRTS":  return new BRTSFareCalculator();
            default:      return new BRTSFareCalculator();
        }
    }
}

class AMTSFareCalculator implements FareCalculator {

    static double slabFare(int numStations) {
        if (numStations <= 0) return 0.0;
        return Math.ceil(numStations / 5.0) * 5.0;
    }

    @Override
    public double calculateFare(int numStations, UserSession user) {
        if (numStations <= 0) return 0.0;

        if (user.getAge() > 65) {
            return 0.0;
        }

        double fare = slabFare(numStations);

        if (user.getAge() < 13) {
            fare = Math.ceil(fare / 2.0);
        }

        if (user.getGender() == 'F') {
            if (fare > 20.0) {
                fare = 20.0;
            }
        } else {
            if (fare > 45.0) {
                fare = 45.0;
            }
        }

        return fare;
    }

    @Override
    public String getFareCalculationBreakdown(int numStations, UserSession user) {
        if (numStations <= 0) return "No stations traveled. Fare: Rs 0";

        if (user.getAge() > 65) {
            return "Senior citizen (age " + user.getAge() + " > 65): free travel, Rs 0";
        }

        double baseFare = slabFare(numStations);
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("AMTS slab fare for ").append(numStations).append(" stations: Rs ")
                .append(String.format("%.0f", baseFare));

        double fare = baseFare;
        if (user.getAge() < 13) {
            fare = Math.ceil(fare / 2.0);
            breakdown.append(" | child (age < 13) half fare: Rs ").append(String.format("%.0f", fare));
        }

        if (user.getGender() == 'F') {
            if (fare > 20.0) {
                breakdown.append(" | women's fare capped at Rs 20");
                fare = 20.0;
            }
        } else {
            if (fare > 45.0) {
                breakdown.append(" | men's fare capped at Rs 45");
                fare = 45.0;
            }
        }

        return breakdown.append(" | final: Rs ").append(String.format("%.0f", fare)).toString();
    }
}


class MetroFareCalculator implements FareCalculator {

    static double slabFare(int numStations) {
        if (numStations <= 0) return 0.0;
        if (numStations <= 3) return 5.0;
        return 5.0 + Math.ceil((numStations - 3) / 4.0) * 5.0;
    }

    @Override
    public double calculateFare(int numStations, UserSession user) {
        if (numStations <= 0) return 0.0;

        if (user.getHeight() < 3.0) {
            return 0.0;
        }

        double fare = slabFare(numStations);
        if (user.hasMetroCard()) {
            fare = Math.round(fare * 0.90 * 2.0) / 2.0; // round to nearest 0.50
        }

        return fare;
    }

    @Override
    public String getFareCalculationBreakdown(int numStations, UserSession user) {
        if (numStations <= 0) return "No stations traveled. Fare: Rs 0";

        if (user.getHeight() < 3.0) {
            return "Height (" + user.getHeight() + " ft < 3 ft): free travel for this passenger, Rs 0";
        }

        double baseFare = slabFare(numStations);
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Metro slab fare for ").append(numStations).append(" stations: Rs ")
                .append(String.format("%.0f", baseFare));

        double fare = baseFare;
        if (user.hasMetroCard()) {
            fare = Math.round(fare * 0.90 * 2.0) / 2.0;
            breakdown.append(" | metro card 10% off: Rs ").append(String.format("%.1f", fare));
        } else {
            breakdown.append(" | token fare, no card discount");
        }

        return breakdown.append(" | final: Rs ").append(String.format("%.1f", fare)).toString();
    }
}


class BRTSFareCalculator implements FareCalculator {

    static double slabFare(int numStations) {
        if (numStations <= 0) return 0.0;
        return Math.ceil(numStations / 5.0) * 5.0;
    }

    @Override
    public double calculateFare(int numStations, UserSession user) {
        return slabFare(numStations);
    }

    @Override
    public String getFareCalculationBreakdown(int numStations, UserSession user) {
        if (numStations <= 0) return "No stations traveled. Fare: Rs 0";
        double fare = slabFare(numStations);
        return "BRTS slab fare for " + numStations + " stations: Rs " + String.format("%.0f", fare)
                + " | final: Rs " + String.format("%.0f", fare);
    }
}