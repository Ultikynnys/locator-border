package dev.liqw.locatorborder.client;

final class HandshakeState {

    private final int retryIntervalTicks;
    private boolean acknowledged;
    private int ticksUntilRetry;
    private int attempts;

    HandshakeState(int retryIntervalTicks) {
        if (retryIntervalTicks < 1) throw new IllegalArgumentException("retryIntervalTicks must be positive");
        this.retryIntervalTicks = retryIntervalTicks;
    }

    synchronized void reset() {
        acknowledged = false;
        ticksUntilRetry = 0;
        attempts = 0;
    }

    synchronized boolean shouldSend(boolean connectionReady) {
        if (!connectionReady || acknowledged) return false;
        if (ticksUntilRetry > 0) {
            ticksUntilRetry--;
            return false;
        }
        ticksUntilRetry = retryIntervalTicks - 1;
        attempts++;
        return true;
    }

    synchronized boolean acknowledge() {
        if (acknowledged) return false;
        acknowledged = true;
        return true;
    }

    synchronized int getAttempts() {
        return attempts;
    }

    synchronized boolean isAcknowledged() {
        return acknowledged;
    }
}
