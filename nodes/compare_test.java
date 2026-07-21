package nodes;

import axiom.AxiomContext;
import gen.Messages.CompareInput;
import gen.Messages.CompareResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CompareTest {

    private static gen.Messages.MoneyAmount money(String amount, String currency) {
        return gen.Messages.MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void aGreaterThanB() {
        AxiomContext ax = TestSupport.newContext();
        CompareResult result = Compare.compare(ax, CompareInput.newBuilder()
            .setA(money("5.00", "USD")).setB(money("3.00", "USD")).build());
        assertEquals("", result.getError().getCode());
        assertEquals(1, result.getComparison());
        assertTrue(result.getGreater());
        assertFalse(result.getEqual());
        assertFalse(result.getLess());
    }

    @Test
    public void aLessThanB() {
        AxiomContext ax = TestSupport.newContext();
        CompareResult result = Compare.compare(ax, CompareInput.newBuilder()
            .setA(money("3.00", "USD")).setB(money("5.00", "USD")).build());
        assertEquals(-1, result.getComparison());
        assertTrue(result.getLess());
    }

    @Test
    public void equalByValue_ignoresTrailingZeroDifference() {
        // 5.0 and 5.00 are the same numeric value despite different scale —
        // an independent property of decimal comparison, not this code.
        AxiomContext ax = TestSupport.newContext();
        CompareResult result = Compare.compare(ax, CompareInput.newBuilder()
            .setA(money("5.0", "USD")).setB(money("5.00", "USD")).build());
        assertEquals(0, result.getComparison());
        assertTrue(result.getEqual());
        assertFalse(result.getGreater());
        assertFalse(result.getLess());
    }

    @Test
    public void currencyMismatch_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        CompareResult result = Compare.compare(ax, CompareInput.newBuilder()
            .setA(money("5.00", "USD")).setB(money("5.00", "EUR")).build());
        assertEquals("CURRENCY_MISMATCH", result.getError().getCode());
    }
}
