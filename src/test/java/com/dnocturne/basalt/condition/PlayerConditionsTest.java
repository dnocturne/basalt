package com.dnocturne.basalt.condition;

import com.dnocturne.basalt.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PlayerConditions factory and condition composition.
 */
@DisplayName("PlayerConditions")
class PlayerConditionsTest {

    private ServerMock server;
    private WorldMock world;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test_world");
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Time-based Conditions")
    class TimeConditions {

        @Test
        @DisplayName("isDay() returns true during daytime")
        void isDay_duringDay_returnsTrue() {
            world.setTime(6000L); // Noon
            assertTrue(PlayerConditions.isDay().test(player));
        }

        @Test
        @DisplayName("isDay() returns false during nighttime")
        void isDay_duringNight_returnsFalse() {
            world.setTime(18000L); // Midnight
            assertFalse(PlayerConditions.isDay().test(player));
        }

        @Test
        @DisplayName("isNight() returns true during nighttime")
        void isNight_duringNight_returnsTrue() {
            world.setTime(18000L); // Midnight
            assertTrue(PlayerConditions.isNight().test(player));
        }

        @Test
        @DisplayName("isNight() returns false during daytime")
        void isNight_duringDay_returnsFalse() {
            world.setTime(6000L); // Noon
            assertFalse(PlayerConditions.isNight().test(player));
        }

        @Test
        @DisplayName("isTimeInRange() works for normal range")
        void isTimeInRange_normalRange_works() {
            Condition<Player> inRange = PlayerConditions.isTimeInRange(6000L, 12000L);

            world.setTime(6000L);
            assertTrue(inRange.test(player));

            world.setTime(9000L);
            assertTrue(inRange.test(player));

            world.setTime(12000L);
            assertTrue(inRange.test(player));

            world.setTime(5999L);
            assertFalse(inRange.test(player));

            world.setTime(12001L);
            assertFalse(inRange.test(player));
        }

        @Test
        @DisplayName("isTimeInRange() works for wrap-around range")
        void isTimeInRange_wrapAround_works() {
            // Range from 22000 to 2000 (spans midnight)
            Condition<Player> inRange = PlayerConditions.isTimeInRange(22000L, 2000L);

            world.setTime(22000L);
            assertTrue(inRange.test(player));

            world.setTime(23000L);
            assertTrue(inRange.test(player));

            world.setTime(0L);
            assertTrue(inRange.test(player));

            world.setTime(2000L);
            assertTrue(inRange.test(player));

            world.setTime(10000L);
            assertFalse(inRange.test(player));
        }
    }

    @Nested
    @DisplayName("Moon Phase Conditions")
    class MoonPhaseConditions {

        @Test
        @DisplayName("isFullMoon() returns true on full moon")
        void isFullMoon_onFullMoon_returnsTrue() {
            world.setFullTime(0); // Day 0 = Full Moon
            assertTrue(PlayerConditions.isFullMoon().test(player));
        }

        @Test
        @DisplayName("isFullMoon() returns false on new moon")
        void isFullMoon_onNewMoon_returnsFalse() {
            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY); // Day 4 = New Moon
            assertFalse(PlayerConditions.isFullMoon().test(player));
        }

        @Test
        @DisplayName("isNewMoon() returns true on new moon")
        void isNewMoon_onNewMoon_returnsTrue() {
            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY); // Day 4 = New Moon
            assertTrue(PlayerConditions.isNewMoon().test(player));
        }

        @Test
        @DisplayName("isMoonPhase() matches specified phases")
        void isMoonPhase_matchesSpecifiedPhases() {
            Condition<Player> condition = PlayerConditions.isMoonPhase(
                    TimeUtil.MoonPhase.FULL_MOON,
                    TimeUtil.MoonPhase.NEW_MOON
            );

            world.setFullTime(0); // Full Moon
            assertTrue(condition.test(player));

            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY); // New Moon
            assertTrue(condition.test(player));

            world.setFullTime(2 * TimeUtil.TICKS_PER_DAY); // Third Quarter
            assertFalse(condition.test(player));
        }

        @Test
        @DisplayName("isBrightMoon() returns true for bright phases")
        void isBrightMoon_forBrightPhases_returnsTrue() {
            // Full Moon (brightness 1.0)
            world.setFullTime(0);
            assertTrue(PlayerConditions.isBrightMoon().test(player));

            // Third Quarter (brightness 0.5)
            world.setFullTime(2 * TimeUtil.TICKS_PER_DAY);
            assertTrue(PlayerConditions.isBrightMoon().test(player));
        }

        @Test
        @DisplayName("isBrightMoon() returns false for dark phases")
        void isBrightMoon_forDarkPhases_returnsFalse() {
            // New Moon (brightness 0.0)
            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY);
            assertFalse(PlayerConditions.isBrightMoon().test(player));

            // Waning Crescent (brightness 0.25)
            world.setFullTime(3 * TimeUtil.TICKS_PER_DAY);
            assertFalse(PlayerConditions.isBrightMoon().test(player));
        }

        @Test
        @DisplayName("isDarkMoon() is the negation of isBrightMoon()")
        void isDarkMoon_isNegationOfBrightMoon() {
            world.setFullTime(0); // Full Moon
            assertFalse(PlayerConditions.isDarkMoon().test(player));

            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY); // New Moon
            assertTrue(PlayerConditions.isDarkMoon().test(player));
        }
    }

    @Nested
    @DisplayName("Equipment Conditions")
    class EquipmentConditions {

        @Test
        @DisplayName("hasHelmet() returns true when wearing helmet")
        void hasHelmet_withHelmet_returnsTrue() {
            PlayerInventory inventory = player.getInventory();
            inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
            assertTrue(PlayerConditions.hasHelmet().test(player));
        }

        @Test
        @DisplayName("hasHelmet() returns false without helmet")
        void hasHelmet_withoutHelmet_returnsFalse() {
            PlayerInventory inventory = player.getInventory();
            inventory.setHelmet(null);
            assertFalse(PlayerConditions.hasHelmet().test(player));
        }

        @Test
        @DisplayName("noHelmet() is negation of hasHelmet()")
        void noHelmet_isNegationOfHasHelmet() {
            PlayerInventory inventory = player.getInventory();

            inventory.setHelmet(null);
            assertTrue(PlayerConditions.noHelmet().test(player));

            inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            assertFalse(PlayerConditions.noHelmet().test(player));
        }
    }

    @Nested
    @DisplayName("World Environment Conditions")
    class WorldEnvironmentConditions {

        @Test
        @DisplayName("isInOverworld() returns true in overworld")
        void isInOverworld_inOverworld_returnsTrue() {
            // Default world is overworld
            assertTrue(PlayerConditions.isInOverworld().test(player));
        }

        @Test
        @DisplayName("isInOverworld() returns false in nether")
        void isInOverworld_inNether_returnsFalse() {
            WorldMock nether = new WorldMock(Material.NETHERRACK, 4);
            nether.setEnvironment(World.Environment.NETHER);
            server.addWorld(nether);
            player.teleport(nether.getSpawnLocation());

            assertFalse(PlayerConditions.isInOverworld().test(player));
            assertTrue(PlayerConditions.isInNether().test(player));
        }

        @Test
        @DisplayName("isInEnd() returns true in the end")
        void isInEnd_inEnd_returnsTrue() {
            WorldMock end = new WorldMock(Material.END_STONE, 4);
            end.setEnvironment(World.Environment.THE_END);
            server.addWorld(end);
            player.teleport(end.getSpawnLocation());

            assertTrue(PlayerConditions.isInEnd().test(player));
            assertFalse(PlayerConditions.isInOverworld().test(player));
        }
    }

    @Nested
    @DisplayName("Weather Conditions")
    class WeatherConditions {

        @Test
        @DisplayName("hasStorm() returns true during storm")
        void hasStorm_duringStorm_returnsTrue() {
            world.setStorm(true);
            assertTrue(PlayerConditions.hasStorm().test(player));
        }

        @Test
        @DisplayName("hasStorm() returns false when clear")
        void hasStorm_whenClear_returnsFalse() {
            world.setStorm(false);
            assertFalse(PlayerConditions.hasStorm().test(player));
        }

        @Test
        @DisplayName("isClearWeather() is negation of hasStorm()")
        void isClearWeather_isNegationOfHasStorm() {
            world.setStorm(true);
            assertFalse(PlayerConditions.isClearWeather().test(player));

            world.setStorm(false);
            assertTrue(PlayerConditions.isClearWeather().test(player));
        }

        @Test
        @DisplayName("isThundering() returns true during thunder")
        void isThundering_duringThunder_returnsTrue() {
            world.setThundering(true);
            assertTrue(PlayerConditions.isThundering().test(player));
        }
    }

    @Nested
    @DisplayName("Composite Conditions")
    class CompositeConditionsTest {

        @Test
        @DisplayName("isFullMoonNight() requires both night and full moon")
        void isFullMoonNight_requiresBoth() {
            Condition<Player> condition = PlayerConditions.isFullMoonNight();

            // Full moon but daytime
            world.setFullTime(0);
            world.setTime(6000L);
            assertFalse(condition.test(player));

            // Night but not full moon
            world.setFullTime(4 * TimeUtil.TICKS_PER_DAY);
            world.setTime(18000L);
            assertFalse(condition.test(player));

            // Full moon AND night
            world.setFullTime(18000L); // Day 0, time 18000
            assertTrue(condition.test(player));
        }
    }

    @Nested
    @DisplayName("Condition Description")
    class ConditionDescription {

        @Test
        @DisplayName("conditions have meaningful descriptions")
        void conditions_haveMeaningfulDescriptions() {
            assertEquals("isDay", PlayerConditions.isDay().describe());
            assertEquals("isNight", PlayerConditions.isNight().describe());
            assertEquals("isFullMoon", PlayerConditions.isFullMoon().describe());
            assertEquals("hasHelmet", PlayerConditions.hasHelmet().describe());
        }
    }
}
