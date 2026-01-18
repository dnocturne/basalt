package com.dnocturne.basalt.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TimeUtil utility class.
 */
@DisplayName("TimeUtil")
class TimeUtilTest {

    private ServerMock server;
    private WorldMock world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test_world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Day/Night Detection")
    class DayNightDetection {

        @ParameterizedTest(name = "time {0} should be day")
        @ValueSource(longs = {0L, 1000L, 6000L, 12000L, 12999L, 23001L, 23500L})
        void isDay_duringDaytime_returnsTrue(long time) {
            world.setTime(time);
            assertTrue(TimeUtil.isDay(world));
            assertFalse(TimeUtil.isNight(world));
        }

        @ParameterizedTest(name = "time {0} should be night")
        @ValueSource(longs = {13000L, 15000L, 18000L, 21000L, 23000L})
        void isNight_duringNighttime_returnsTrue(long time) {
            world.setTime(time);
            assertTrue(TimeUtil.isNight(world));
            assertFalse(TimeUtil.isDay(world));
        }

        @Test
        @DisplayName("sunrise (0) is daytime")
        void sunrise_isDay() {
            world.setTime(TimeUtil.SUNRISE);
            assertTrue(TimeUtil.isDay(world));
        }

        @Test
        @DisplayName("noon (6000) is daytime")
        void noon_isDay() {
            world.setTime(TimeUtil.NOON);
            assertTrue(TimeUtil.isDay(world));
        }

        @Test
        @DisplayName("sunset (12000) is daytime")
        void sunset_isDay() {
            world.setTime(TimeUtil.SUNSET);
            assertTrue(TimeUtil.isDay(world));
        }

        @Test
        @DisplayName("midnight (18000) is nighttime")
        void midnight_isNight() {
            world.setTime(TimeUtil.MIDNIGHT);
            assertTrue(TimeUtil.isNight(world));
        }

        @Test
        @DisplayName("night starts at 13000")
        void nightStart_isNight() {
            world.setTime(TimeUtil.NIGHT_START);
            assertTrue(TimeUtil.isNight(world));
        }

        @Test
        @DisplayName("night ends at 23000")
        void nightEnd_isNight() {
            world.setTime(TimeUtil.NIGHT_END);
            assertTrue(TimeUtil.isNight(world));
        }
    }

    @Nested
    @DisplayName("Moon Phase Calculation")
    class MoonPhaseCalculation {

        @ParameterizedTest(name = "day {0} should have moon phase {1}")
        @CsvSource({
                "0, 0",   // Day 0 = Full Moon
                "1, 1",   // Day 1 = Waning Gibbous
                "2, 2",   // Day 2 = Third Quarter
                "3, 3",   // Day 3 = Waning Crescent
                "4, 4",   // Day 4 = New Moon
                "5, 5",   // Day 5 = Waxing Crescent
                "6, 6",   // Day 6 = First Quarter
                "7, 7",   // Day 7 = Waxing Gibbous
                "8, 0",   // Day 8 = Full Moon (cycle repeats)
                "16, 0",  // Day 16 = Full Moon
                "12, 4",  // Day 12 = New Moon
        })
        void getMoonPhase_returnsCorrectPhase(long day, int expectedPhase) {
            world.setFullTime(day * TimeUtil.TICKS_PER_DAY);
            assertEquals(expectedPhase, TimeUtil.getMoonPhase(world));
        }

        @Test
        @DisplayName("isFullMoon returns true on day 0")
        void isFullMoon_onDay0_returnsTrue() {
            world.setFullTime(0);
            assertTrue(TimeUtil.isFullMoon(world));
        }

        @Test
        @DisplayName("isFullMoon returns false on day 4")
        void isFullMoon_onDay4_returnsFalse() {
            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY);
            assertFalse(TimeUtil.isFullMoon(world));
        }

        @Test
        @DisplayName("isNewMoon returns true on day 4")
        void isNewMoon_onDay4_returnsTrue() {
            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY);
            assertTrue(TimeUtil.isNewMoon(world));
        }

        @Test
        @DisplayName("isNewMoon returns false on day 0")
        void isNewMoon_onDay0_returnsFalse() {
            world.setFullTime(0);
            assertFalse(TimeUtil.isNewMoon(world));
        }

        @Test
        @DisplayName("getMoonPhaseEnum returns correct enum")
        void getMoonPhaseEnum_returnsCorrectEnum() {
            world.setFullTime(0);
            assertEquals(TimeUtil.MoonPhase.FULL_MOON, TimeUtil.getMoonPhaseEnum(world));

            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY);
            assertEquals(TimeUtil.MoonPhase.NEW_MOON, TimeUtil.getMoonPhaseEnum(world));
        }
    }

    @Nested
    @DisplayName("Day Number Calculation")
    class DayNumberCalculation {

        @Test
        @DisplayName("day 0 at tick 0")
        void getDayNumber_atTick0_returns0() {
            world.setFullTime(0);
            assertEquals(0, TimeUtil.getDayNumber(world));
        }

        @Test
        @DisplayName("still day 0 at tick 23999")
        void getDayNumber_atTick23999_returns0() {
            world.setFullTime(23999);
            assertEquals(0, TimeUtil.getDayNumber(world));
        }

        @Test
        @DisplayName("day 1 at tick 24000")
        void getDayNumber_atTick24000_returns1() {
            world.setFullTime(24000);
            assertEquals(1, TimeUtil.getDayNumber(world));
        }

        @Test
        @DisplayName("day 10 at tick 240000")
        void getDayNumber_atTick240000_returns10() {
            world.setFullTime(240000);
            assertEquals(10, TimeUtil.getDayNumber(world));
        }
    }

    @Nested
    @DisplayName("Time Conversions")
    class TimeConversions {

        @Test
        @DisplayName("ticksToSeconds converts correctly")
        void ticksToSeconds_convertsCorrectly() {
            assertEquals(0, TimeUtil.ticksToSeconds(0));
            assertEquals(1, TimeUtil.ticksToSeconds(20));
            assertEquals(60, TimeUtil.ticksToSeconds(1200));
            assertEquals(3600, TimeUtil.ticksToSeconds(72000));
        }

        @Test
        @DisplayName("secondsToTicks converts correctly")
        void secondsToTicks_convertsCorrectly() {
            assertEquals(0, TimeUtil.secondsToTicks(0));
            assertEquals(20, TimeUtil.secondsToTicks(1));
            assertEquals(1200, TimeUtil.secondsToTicks(60));
            assertEquals(72000, TimeUtil.secondsToTicks(3600));
        }

        @Test
        @DisplayName("formatDuration formats seconds only")
        void formatDuration_secondsOnly() {
            assertEquals("0s", TimeUtil.formatDuration(0));
            assertEquals("1s", TimeUtil.formatDuration(20));
            assertEquals("30s", TimeUtil.formatDuration(600));
        }

        @Test
        @DisplayName("formatDuration formats minutes and seconds")
        void formatDuration_minutesAndSeconds() {
            assertEquals("1m 0s", TimeUtil.formatDuration(1200));
            assertEquals("1m 30s", TimeUtil.formatDuration(1800));
            assertEquals("5m 15s", TimeUtil.formatDuration(6300));
        }

        @Test
        @DisplayName("formatDuration formats hours, minutes and seconds")
        void formatDuration_hoursMinutesAndSeconds() {
            assertEquals("1h 0m 0s", TimeUtil.formatDuration(72000));   // 3600s = 1h
            assertEquals("1h 31m 15s", TimeUtil.formatDuration(109500)); // 5475s = 1h 31m 15s
            assertEquals("2h 15m 30s", TimeUtil.formatDuration(162600)); // 8130s = 2h 15m 30s
        }
    }

    @Nested
    @DisplayName("MoonPhase Enum")
    class MoonPhaseEnumTest {

        @Test
        @DisplayName("fromPhase returns correct enum")
        void fromPhase_returnsCorrectEnum() {
            assertEquals(TimeUtil.MoonPhase.FULL_MOON, TimeUtil.MoonPhase.fromPhase(0));
            assertEquals(TimeUtil.MoonPhase.WANING_GIBBOUS, TimeUtil.MoonPhase.fromPhase(1));
            assertEquals(TimeUtil.MoonPhase.THIRD_QUARTER, TimeUtil.MoonPhase.fromPhase(2));
            assertEquals(TimeUtil.MoonPhase.WANING_CRESCENT, TimeUtil.MoonPhase.fromPhase(3));
            assertEquals(TimeUtil.MoonPhase.NEW_MOON, TimeUtil.MoonPhase.fromPhase(4));
            assertEquals(TimeUtil.MoonPhase.WAXING_CRESCENT, TimeUtil.MoonPhase.fromPhase(5));
            assertEquals(TimeUtil.MoonPhase.FIRST_QUARTER, TimeUtil.MoonPhase.fromPhase(6));
            assertEquals(TimeUtil.MoonPhase.WAXING_GIBBOUS, TimeUtil.MoonPhase.fromPhase(7));
        }

        @Test
        @DisplayName("fromPhase returns FULL_MOON for invalid phase")
        void fromPhase_invalidPhase_returnsFallback() {
            assertEquals(TimeUtil.MoonPhase.FULL_MOON, TimeUtil.MoonPhase.fromPhase(8));
            assertEquals(TimeUtil.MoonPhase.FULL_MOON, TimeUtil.MoonPhase.fromPhase(-1));
        }

        @Test
        @DisplayName("getBrightness returns correct values")
        void getBrightness_returnsCorrectValues() {
            assertEquals(1.0f, TimeUtil.MoonPhase.FULL_MOON.getBrightness());
            assertEquals(0.75f, TimeUtil.MoonPhase.WANING_GIBBOUS.getBrightness());
            assertEquals(0.75f, TimeUtil.MoonPhase.WAXING_GIBBOUS.getBrightness());
            assertEquals(0.5f, TimeUtil.MoonPhase.THIRD_QUARTER.getBrightness());
            assertEquals(0.5f, TimeUtil.MoonPhase.FIRST_QUARTER.getBrightness());
            assertEquals(0.25f, TimeUtil.MoonPhase.WANING_CRESCENT.getBrightness());
            assertEquals(0.25f, TimeUtil.MoonPhase.WAXING_CRESCENT.getBrightness());
            assertEquals(0.0f, TimeUtil.MoonPhase.NEW_MOON.getBrightness());
        }

        @Test
        @DisplayName("isBright returns true for phases >= 50% brightness")
        void isBright_returnsCorrectly() {
            assertTrue(TimeUtil.MoonPhase.FULL_MOON.isBright());
            assertTrue(TimeUtil.MoonPhase.WANING_GIBBOUS.isBright());
            assertTrue(TimeUtil.MoonPhase.WAXING_GIBBOUS.isBright());
            assertTrue(TimeUtil.MoonPhase.THIRD_QUARTER.isBright());
            assertTrue(TimeUtil.MoonPhase.FIRST_QUARTER.isBright());
            assertFalse(TimeUtil.MoonPhase.WANING_CRESCENT.isBright());
            assertFalse(TimeUtil.MoonPhase.WAXING_CRESCENT.isBright());
            assertFalse(TimeUtil.MoonPhase.NEW_MOON.isBright());
        }

        @Test
        @DisplayName("getDisplayName returns human-readable names")
        void getDisplayName_returnsReadableNames() {
            assertEquals("Full Moon", TimeUtil.MoonPhase.FULL_MOON.getDisplayName());
            assertEquals("New Moon", TimeUtil.MoonPhase.NEW_MOON.getDisplayName());
            assertEquals("Waning Gibbous", TimeUtil.MoonPhase.WANING_GIBBOUS.getDisplayName());
        }
    }

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("constants have correct values")
        void constants_haveCorrectValues() {
            assertEquals(20L, TimeUtil.TICKS_PER_SECOND);
            assertEquals(1200L, TimeUtil.TICKS_PER_MINUTE);
            assertEquals(72000L, TimeUtil.TICKS_PER_HOUR);
            assertEquals(24000L, TimeUtil.TICKS_PER_DAY);

            assertEquals(0L, TimeUtil.SUNRISE);
            assertEquals(6000L, TimeUtil.NOON);
            assertEquals(12000L, TimeUtil.SUNSET);
            assertEquals(18000L, TimeUtil.MIDNIGHT);
            assertEquals(13000L, TimeUtil.NIGHT_START);
            assertEquals(23000L, TimeUtil.NIGHT_END);
        }
    }
}
