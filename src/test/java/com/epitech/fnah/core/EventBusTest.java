package com.epitech.fnah.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    private EventBus bus;

    @BeforeEach
    void setUp() {
        bus = new EventBus();
    }

    @Test
    void deliversToSubscribersOfTheType() {
        List<Integer> hours = new ArrayList<>();
        bus.subscribe(GameEvent.HourChanged.class, e -> hours.add(e.hour()));
        bus.publish(new GameEvent.HourChanged(3));
        assertEquals(List.of(3), hours);
    }

    @Test
    void ignoresOtherTypes() {
        List<GameEvent> seen = new ArrayList<>();
        bus.subscribe(GameEvent.HourChanged.class, seen::add);
        bus.publish(new GameEvent.StateChanged(GameState.WIN));
        assertTrue(seen.isEmpty());
    }

    @Test
    void publishingWithoutSubscribersIsFine() {
        bus.publish(new GameEvent.HourChanged(1));
    }

    @Test
    void deliversInSubscriptionOrder() {
        StringBuilder order = new StringBuilder();
        bus.subscribe(GameEvent.HourChanged.class, e -> order.append("a"));
        bus.subscribe(GameEvent.HourChanged.class, e -> order.append("b"));
        bus.publish(new GameEvent.HourChanged(1));
        assertEquals("ab", order.toString());
    }

    @Test
    void subscribingDuringPublishDoesNotBreak() {
        List<Integer> late = new ArrayList<>();
        bus.subscribe(GameEvent.HourChanged.class,
                e -> bus.subscribe(GameEvent.HourChanged.class, x -> late.add(x.hour())));
        bus.publish(new GameEvent.HourChanged(1));
        assertTrue(late.isEmpty());
        bus.publish(new GameEvent.HourChanged(2));
        assertEquals(List.of(2), late);
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> bus.publish(null));
        assertThrows(NullPointerException.class, () -> bus.subscribe(null, e -> { }));
        assertThrows(NullPointerException.class, () -> bus.subscribe(GameEvent.HourChanged.class, null));
    }
}
