package nodes;

import axiom.AxiomContext;
import gen.Messages.SignInput;
import gen.Messages.SignResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SignTest {

    private static gen.Messages.MoneyAmount money(String amount, String currency) {
        return gen.Messages.MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void positiveAmount() {
        AxiomContext ax = TestSupport.newContext();
        SignResult result = Sign.sign(ax, SignInput.newBuilder().setAmount(money("5.00", "USD")).build());
        assertEquals("", result.getError().getCode());
        assertTrue(result.getIsPositive());
        assertFalse(result.getIsNegative());
        assertFalse(result.getIsZero());
        assertEquals(1, result.getSignum());
    }

    @Test
    public void negativeAmount() {
        AxiomContext ax = TestSupport.newContext();
        SignResult result = Sign.sign(ax, SignInput.newBuilder().setAmount(money("-5.00", "USD")).build());
        assertTrue(result.getIsNegative());
        assertFalse(result.getIsPositive());
        assertEquals(-1, result.getSignum());
    }

    @Test
    public void zeroAmount_anyScaleStillZero() {
        AxiomContext ax = TestSupport.newContext();
        SignResult result = Sign.sign(ax, SignInput.newBuilder().setAmount(money("0.00", "USD")).build());
        assertTrue(result.getIsZero());
        assertFalse(result.getIsPositive());
        assertFalse(result.getIsNegative());
        assertEquals(0, result.getSignum());
        // Regression guard (caught by review): signum is a proto3
        // `optional int32` specifically so the zero result is still PRESENT
        // on the wire — a plain int32 field silently vanishes from JSON at
        // its zero default. hasSignum() only exists because the field is
        // `optional`; reverting that in messages.proto breaks this build.
        assertTrue(result.hasSignum());
    }
}
