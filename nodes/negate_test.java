package nodes;

import axiom.AxiomContext;
import gen.Messages.NegateInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NegateTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void flipsPositiveToNegative() {
        AxiomContext ax = TestSupport.newContext();
        MoneyAmount result = Negate.negate(ax, NegateInput.newBuilder().setAmount(money("4.50", "USD")).build());
        assertEquals("", result.getError().getCode());
        assertEquals("-4.50", result.getAmount());
    }

    @Test
    public void roundTrip_negateTwiceIsIdentity() {
        AxiomContext ax = TestSupport.newContext();
        MoneyAmount once = Negate.negate(ax, NegateInput.newBuilder().setAmount(money("4.50", "USD")).build());
        MoneyAmount twice = Negate.negate(ax, NegateInput.newBuilder().setAmount(money(once.getAmount(), "USD")).build());
        assertEquals("4.50", twice.getAmount());
    }

    @Test
    public void malformedAmount_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        MoneyAmount result = Negate.negate(ax, NegateInput.newBuilder().setAmount(money("abc", "USD")).build());
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }
}
