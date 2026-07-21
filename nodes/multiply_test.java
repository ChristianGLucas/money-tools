package nodes;

import axiom.AxiomContext;
import gen.Messages.MultiplyInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultiplyTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void multipliesByIntegerFactor_handComputed() {
        AxiomContext ax = TestSupport.newContext();
        MultiplyInput input = MultiplyInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setFactor("3")
            .build();
        MoneyAmount result = Multiply.multiply(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("30.00", result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    public void multipliesByDecimalFactor_exactPrecisionPreserved() {
        // 19.99 * 3 = 59.97 hand-computed; also checks scale accumulates
        // (2 decimal places * 0 decimal places = 2 decimal places).
        AxiomContext ax = TestSupport.newContext();
        MultiplyInput input = MultiplyInput.newBuilder()
            .setAmount(money("19.99", "USD"))
            .setFactor("3")
            .build();
        MoneyAmount result = Multiply.multiply(ax, input);
        assertEquals("59.97", result.getAmount());
    }

    @Test
    public void malformedFactor_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        MultiplyInput input = MultiplyInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setFactor("not-a-number")
            .build();
        MoneyAmount result = Multiply.multiply(ax, input);
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }
}
