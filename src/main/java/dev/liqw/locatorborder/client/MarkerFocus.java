package dev.liqw.locatorborder.client;

final class MarkerFocus {

    static final double AIM_ALIGNMENT = Math.cos(Math.toRadians(6.0D));

    private MarkerFocus() {}

    static double alignment(double lookX, double lookY, double lookZ, double targetX, double targetY, double targetZ) {
        double lookLength = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
        double targetLength = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
        if (lookLength == 0.0D || targetLength == 0.0D) return -1.0D;
        return (lookX * targetX + lookY * targetY + lookZ * targetZ) / (lookLength * targetLength);
    }

    static boolean isFocused(double alignment, double minimumAlignment) {
        return alignment >= minimumAlignment;
    }

    static boolean reveal(FocusTrigger trigger, boolean aimed, boolean playerListPressed) {
        if (trigger == null) throw new IllegalArgumentException("Focus trigger cannot be null");
        return trigger.reveals(aimed, playerListPressed);
    }

    static boolean isBetter(double alignment, double distanceSquared, double bestAlignment,
        double bestDistanceSquared) {
        return alignment > bestAlignment || alignment == bestAlignment && distanceSquared < bestDistanceSquared;
    }
}
