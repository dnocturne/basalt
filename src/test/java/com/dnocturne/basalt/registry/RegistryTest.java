package com.dnocturne.basalt.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the generic Registry system.
 */
@DisplayName("Registry")
class RegistryTest {

    private Registry<TestItem> registry;

    @BeforeEach
    void setUp() {
        registry = Registry.forIdentifiable("item");
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("register adds item to registry")
        void register_addsItem() {
            TestItem item = new TestItem("test", "Test Item");
            registry.register(item);

            assertTrue(registry.isRegistered("test"));
            assertEquals(1, registry.size());
        }

        @Test
        @DisplayName("register is case-insensitive")
        void register_caseInsensitive() {
            TestItem item = new TestItem("TEST", "Test Item");
            registry.register(item);

            assertTrue(registry.isRegistered("test"));
            assertTrue(registry.isRegistered("TEST"));
            assertTrue(registry.isRegistered("TeSt"));
        }

        @Test
        @DisplayName("register throws on duplicate ID")
        void register_duplicateThrows() {
            TestItem first = new TestItem("dupe", "First");
            TestItem second = new TestItem("dupe", "Second");

            registry.register(first);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> registry.register(second)
            );

            assertTrue(ex.getMessage().contains("dupe"));
            assertTrue(ex.getMessage().contains("conflict"));
        }

        @Test
        @DisplayName("registerOrReplace replaces existing item")
        void registerOrReplace_replacesExisting() {
            TestItem first = new TestItem("replace", "First");
            TestItem second = new TestItem("replace", "Second");

            registry.register(first);
            TestItem previous = registry.registerOrReplace(second);

            assertSame(first, previous);
            assertEquals("Second", registry.get("replace").map(TestItem::displayName).orElse(null));
        }

        @Test
        @DisplayName("registerOrReplace returns null for new item")
        void registerOrReplace_newItem_returnsNull() {
            TestItem item = new TestItem("new", "New Item");
            TestItem previous = registry.registerOrReplace(item);

            assertNull(previous);
            assertTrue(registry.isRegistered("new"));
        }
    }

    @Nested
    @DisplayName("Retrieval")
    class Retrieval {

        @Test
        @DisplayName("get returns Optional with registered item")
        void get_registered_returnsOptional() {
            TestItem item = new TestItem("find", "Find Me");
            registry.register(item);

            Optional<TestItem> result = registry.get("find");

            assertTrue(result.isPresent());
            assertSame(item, result.get());
        }

        @Test
        @DisplayName("get returns empty for unregistered ID")
        void get_unregistered_returnsEmpty() {
            Optional<TestItem> result = registry.get("nonexistent");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("get is case-insensitive")
        void get_caseInsensitive() {
            TestItem item = new TestItem("UPPER", "Upper Item");
            registry.register(item);

            assertTrue(registry.get("upper").isPresent());
            assertTrue(registry.get("UPPER").isPresent());
            assertTrue(registry.get("Upper").isPresent());
        }

        @Test
        @DisplayName("getAll returns all registered items")
        void getAll_returnsAll() {
            registry.register(new TestItem("a", "A"));
            registry.register(new TestItem("b", "B"));
            registry.register(new TestItem("c", "C"));

            assertEquals(3, registry.getAll().size());
        }

        @Test
        @DisplayName("getAllIds returns all IDs in lowercase")
        void getAllIds_returnsLowercaseIds() {
            registry.register(new TestItem("UPPER", "Upper"));
            registry.register(new TestItem("Lower", "Lower"));

            var ids = registry.getAllIds();
            assertEquals(2, ids.size());
            assertTrue(ids.contains("upper"));
            assertTrue(ids.contains("lower"));
        }
    }

    @Nested
    @DisplayName("Unregistration")
    class Unregistration {

        @Test
        @DisplayName("unregister removes item")
        void unregister_removesItem() {
            registry.register(new TestItem("remove", "Remove Me"));
            assertTrue(registry.isRegistered("remove"));

            boolean result = registry.unregister("remove");

            assertTrue(result);
            assertFalse(registry.isRegistered("remove"));
        }

        @Test
        @DisplayName("unregister returns false for nonexistent ID")
        void unregister_nonexistent_returnsFalse() {
            boolean result = registry.unregister("nonexistent");
            assertFalse(result);
        }

        @Test
        @DisplayName("unregister is case-insensitive")
        void unregister_caseInsensitive() {
            registry.register(new TestItem("UPPER", "Upper"));

            boolean result = registry.unregister("upper");

            assertTrue(result);
            assertFalse(registry.isRegistered("UPPER"));
        }

        @Test
        @DisplayName("clear removes all items")
        void clear_removesAll() {
            registry.register(new TestItem("a", "A"));
            registry.register(new TestItem("b", "B"));
            assertEquals(2, registry.size());

            registry.clear();

            assertEquals(0, registry.size());
            assertTrue(registry.isEmpty());
        }
    }

    @Nested
    @DisplayName("Size and Empty")
    class SizeAndEmpty {

        @Test
        @DisplayName("size returns correct count")
        void size_returnsCorrectCount() {
            assertEquals(0, registry.size());

            registry.register(new TestItem("a", "A"));
            assertEquals(1, registry.size());

            registry.register(new TestItem("b", "B"));
            assertEquals(2, registry.size());
        }

        @Test
        @DisplayName("isEmpty returns true when empty")
        void isEmpty_whenEmpty_returnsTrue() {
            assertTrue(registry.isEmpty());
        }

        @Test
        @DisplayName("isEmpty returns false when not empty")
        void isEmpty_whenNotEmpty_returnsFalse() {
            registry.register(new TestItem("item", "Item"));
            assertFalse(registry.isEmpty());
        }
    }

    @Nested
    @DisplayName("Custom ID Extractor")
    class CustomIdExtractor {

        @Test
        @DisplayName("works with custom ID extractor")
        void customIdExtractor_works() {
            Registry<String> stringRegistry = new Registry<>(String::toLowerCase, "string");

            stringRegistry.register("HELLO");
            stringRegistry.register("World");

            assertTrue(stringRegistry.isRegistered("hello"));
            assertTrue(stringRegistry.isRegistered("WORLD"));
            assertEquals(2, stringRegistry.size());
        }
    }

    @Nested
    @DisplayName("Display Name Extractor")
    class DisplayNameExtractor {

        @Test
        @DisplayName("error message includes display name")
        void errorMessage_includesDisplayName() {
            registry.setDisplayNameExtractor(TestItem::displayName);

            registry.register(new TestItem("dupe", "First Display"));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> registry.register(new TestItem("dupe", "Second Display"))
            );

            assertTrue(ex.getMessage().contains("First Display"));
            assertTrue(ex.getMessage().contains("Second Display"));
        }
    }

    /**
     * Simple test item implementing Identifiable.
     */
    private record TestItem(String id, String displayName) implements Identifiable {
        @Override
        public String getId() {
            return id;
        }
    }
}
