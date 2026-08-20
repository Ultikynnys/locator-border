package dev.liqw.locatorborder.client;

final class MarkerFocus {

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

    static boolean reveal(String trigger, boolean aimed, boolean playerListPressed) {
        if ("ALWAYS".equals(trigger)) return true;
        if ("HOVER".equals(trigger) || "FOCAL".equals(trigger)) return aimed;
        return "PLAYER_LIST".equals(trigger) && playerListPressed;
    }

    static boolean isBetter(double alignment, double distanceSquared, double bestAlignment,
        double bestDistanceSquared) {
        return alignment > bestAlignment || alignment == bestAlignment && distanceSquared < bestDistanceSquared;
    }
}
