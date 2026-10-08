package com.epitech.fnah.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PowerSystemTest {

    private PowerSystem power;

    @BeforeEach
    void setUp() {
        power = new PowerSystem();
    }

    @Test
    void startsFull() {
        assertEquals(100.0, power.getEnergy());
        assertFalse(power.isPowerOut());
    }

    @Test
    void baseUsageIsOne() {
        assertEquals(1, power.computeUsage());
    }

    @Test
    void consumersAddToUsage() {
        power.addConsumer(() -> 1);
        power.addConsumer(() -> 1);
        assertEquals(3, power.computeUsage());
    }

    @Test
    void drainsPointOnePerSecondAtBaseUsage() {
        power.update(1.0);
        assertEquals(99.9, power.getEnergy(), 1e-9);
    }

    @Test
    void drainsFasterWithConsumers() {
        power.addConsumer(() -> 1);
        power.update(1.0);
        assertEquals(99.8, power.getEnergy(), 1e-9);
    }

    @Test
    void neverGoesBelowZero() {
        power.update(10_000.0);
        assertEquals(0.0, power.getEnergy());
    }

    @Test
    void powerOutFiresExactlyOnce() {
        AtomicInteger count = new AtomicInteger();
        power.onPowerOut(p -> count.incrementAndGet());
        power.update(10_000.0);
        power.update(1.0);
        power.update(1.0);
        assertEquals(1, count.get());
        assertTrue(power.isPowerOut());
    }

    @Test
    void resetRestoresEnergyAndPowerOutFlag() {
        power.update(10_000.0);
        power.reset();
        assertEquals(100.0, power.getEnergy());
        assertFalse(power.isPowerOut());
    }

    @Test
    void rejectsNegativeDelta() {
        assertThrows(IllegalArgumentException.class, () -> power.update(-0.1));
    }

    @Test
    void energyMultiplierScalesDrain() {
        power.setEnergyMultiplier(2.0);
        power.update(1.0);
        assertEquals(99.8, power.getEnergy(), 1e-9);
    }

    @Test
    void fullNightWithBaseUsageCosts36Percent() {
        power.update(360.0);
        assertEquals(64.0, power.getEnergy(), 1e-6);
    }

    @Test
    void fullNightWithOneDoorAlwaysClosedCosts72Percent() {
        power.addConsumer(() -> 1);
        power.update(360.0);
        assertEquals(28.0, power.getEnergy(), 1e-6);
    }
}