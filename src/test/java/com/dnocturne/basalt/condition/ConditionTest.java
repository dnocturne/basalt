package com.dnocturne.basalt.condition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the generic Condition system.
 */
@DisplayName("Condition")
class ConditionTest {

    // Simple test conditions using String as context
    private final Condition<String> alwaysTrue = Condition.always();
    private final Condition<String> alwaysFalse = Condition.never();
    private final Condition<String> isEmpty = String::isEmpty;
    private final Condition<String> startsWithA = s -> s.startsWith("A");

    @Nested
    @DisplayName("Basic Conditions")
    class BasicConditions {

        @Test
        @DisplayName("always() returns true for any input")
        void always_returnsTrue() {
            assertTrue(alwaysTrue.test("anything"));
            assertTrue(alwaysTrue.test(""));
            assertTrue(alwaysTrue.test("test"));
        }

        @Test
        @DisplayName("never() returns false for any input")
        void never_returnsFalse() {
            assertFalse(alwaysFalse.test("anything"));
            assertFalse(alwaysFalse.test(""));
            assertFalse(alwaysFalse.test("test"));
        }

        @Test
        @DisplayName("custom condition works correctly")
        void customCondition_worksCorrectly() {
            assertTrue(isEmpty.test(""));
            assertFalse(isEmpty.test("not empty"));

            assertTrue(startsWithA.test("Apple"));
            assertFalse(startsWithA.test("Banana"));
        }
    }

    @Nested
    @DisplayName("AND Operations")
    class AndOperations {

        @Test
        @DisplayName("AND with all true returns true")
        void and_allTrue_returnsTrue() {
            Condition<String> result = alwaysTrue.and(alwaysTrue);
            assertTrue(result.test("test"));
        }

        @Test
        @DisplayName("AND with one false returns false")
        void and_oneFalse_returnsFalse() {
            Condition<String> result = alwaysTrue.and(alwaysFalse);
            assertFalse(result.test("test"));
        }

        @Test
        @DisplayName("AND with all false returns false")
        void and_allFalse_returnsFalse() {
            Condition<String> result = alwaysFalse.and(alwaysFalse);
            assertFalse(result.test("test"));
        }

        @Test
        @DisplayName("AND chains correctly")
        void and_chainsCorrectly() {
            Condition<String> result = alwaysTrue.and(alwaysTrue).and(alwaysTrue);
            assertTrue(result.test("test"));

            Condition<String> result2 = alwaysTrue.and(alwaysTrue).and(alwaysFalse);
            assertFalse(result2.test("test"));
        }

        @Test
        @DisplayName("CompositeCondition.and with empty returns always")
        void compositeAnd_empty_returnsAlways() {
            Condition<String> result = CompositeCondition.and();
            assertTrue(result.test("test"));
        }

        @Test
        @DisplayName("CompositeCondition.and with single returns same condition")
        @SuppressWarnings("unchecked")
        void compositeAnd_single_returnsSame() {
            Condition<String>[] single = new Condition[] { isEmpty };
            Condition<String> result = CompositeCondition.and(single);
            assertSame(isEmpty, result);
        }
    }

    @Nested
    @DisplayName("OR Operations")
    class OrOperations {

        @Test
        @DisplayName("OR with all false returns false")
        void or_allFalse_returnsFalse() {
            Condition<String> result = alwaysFalse.or(alwaysFalse);
            assertFalse(result.test("test"));
        }

        @Test
        @DisplayName("OR with one true returns true")
        void or_oneTrue_returnsTrue() {
            Condition<String> result = alwaysFalse.or(alwaysTrue);
            assertTrue(result.test("test"));
        }

        @Test
        @DisplayName("OR with all true returns true")
        void or_allTrue_returnsTrue() {
            Condition<String> result = alwaysTrue.or(alwaysTrue);
            assertTrue(result.test("test"));
        }

        @Test
        @DisplayName("OR chains correctly")
        void or_chainsCorrectly() {
            Condition<String> result = alwaysFalse.or(alwaysFalse).or(alwaysTrue);
            assertTrue(result.test("test"));

            Condition<String> result2 = alwaysFalse.or(alwaysFalse).or(alwaysFalse);
            assertFalse(result2.test("test"));
        }

        @Test
        @DisplayName("CompositeCondition.or with empty returns never")
        void compositeOr_empty_returnsNever() {
            Condition<String> result = CompositeCondition.or();
            assertFalse(result.test("test"));
        }

        @Test
        @DisplayName("CompositeCondition.or with single returns same condition")
        @SuppressWarnings("unchecked")
        void compositeOr_single_returnsSame() {
            Condition<String>[] single = new Condition[] { isEmpty };
            Condition<String> result = CompositeCondition.or(single);
            assertSame(isEmpty, result);
        }
    }

    @Nested
    @DisplayName("NOT Operations")
    class NotOperations {

        @Test
        @DisplayName("negate true returns false")
        void negate_true_returnsFalse() {
            Condition<String> result = alwaysTrue.negate();
            assertFalse(result.test("test"));
        }

        @Test
        @DisplayName("negate false returns true")
        void negate_false_returnsTrue() {
            Condition<String> result = alwaysFalse.negate();
            assertTrue(result.test("test"));
        }

        @Test
        @DisplayName("double negation returns original")
        void doubleNegation_returnsOriginal() {
            Condition<String> notNot = CompositeCondition.not(CompositeCondition.not(isEmpty));
            assertSame(isEmpty, notNot);
        }
    }

    @Nested
    @DisplayName("Complex Compositions")
    class ComplexCompositions {

        @Test
        @DisplayName("(A AND B) OR C works correctly")
        void andThenOr_worksCorrectly() {
            // (false AND true) OR true = true
            Condition<String> result = alwaysFalse.and(alwaysTrue).or(alwaysTrue);
            assertTrue(result.test("test"));

            // (false AND true) OR false = false
            Condition<String> result2 = alwaysFalse.and(alwaysTrue).or(alwaysFalse);
            assertFalse(result2.test("test"));
        }

        @Test
        @DisplayName("A OR (B AND C) works correctly")
        void orWithAndGroup_worksCorrectly() {
            // true OR (false AND false) = true
            Condition<String> result = alwaysTrue.or(alwaysFalse.and(alwaysFalse));
            assertTrue(result.test("test"));

            // false OR (true AND true) = true
            Condition<String> result2 = alwaysFalse.or(alwaysTrue.and(alwaysTrue));
            assertTrue(result2.test("test"));

            // false OR (true AND false) = false
            Condition<String> result3 = alwaysFalse.or(alwaysTrue.and(alwaysFalse));
            assertFalse(result3.test("test"));
        }

        @Test
        @DisplayName("NOT (A AND B) works correctly (De Morgan)")
        void notAnd_deMorgan() {
            // NOT (true AND true) = false
            Condition<String> result = alwaysTrue.and(alwaysTrue).negate();
            assertFalse(result.test("test"));

            // NOT (true AND false) = true
            Condition<String> result2 = alwaysTrue.and(alwaysFalse).negate();
            assertTrue(result2.test("test"));
        }
    }

    @Nested
    @DisplayName("Description")
    class Description {

        @Test
        @DisplayName("Condition.of creates described condition")
        void conditionOf_createsDescribed() {
            Condition<String> described = Condition.of("isEmpty", String::isEmpty);
            assertEquals("isEmpty", described.describe());
            assertTrue(described.test(""));
            assertFalse(described.test("not empty"));
        }

        @Test
        @DisplayName("AND description shows all conditions")
        void and_description_showsAll() {
            Condition<String> a = Condition.of("A", s -> true);
            Condition<String> b = Condition.of("B", s -> true);
            Condition<String> result = CompositeCondition.and(a, b);

            String desc = result.describe();
            assertTrue(desc.contains("A"));
            assertTrue(desc.contains("B"));
            assertTrue(desc.contains("AND"));
        }

        @Test
        @DisplayName("OR description shows all conditions")
        void or_description_showsAll() {
            Condition<String> a = Condition.of("A", s -> true);
            Condition<String> b = Condition.of("B", s -> true);
            Condition<String> result = CompositeCondition.or(a, b);

            String desc = result.describe();
            assertTrue(desc.contains("A"));
            assertTrue(desc.contains("B"));
            assertTrue(desc.contains("OR"));
        }

        @Test
        @DisplayName("NOT description shows negation")
        void not_description_showsNegation() {
            Condition<String> a = Condition.of("A", s -> true);
            Condition<String> result = CompositeCondition.not(a);

            String desc = result.describe();
            assertTrue(desc.contains("NOT"));
            assertTrue(desc.contains("A"));
        }
    }
}
