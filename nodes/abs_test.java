package nodes;

import axiom.AxiomContext;
import gen.Messages.AbsInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbsTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void negativeBecomesPositive() {
        AxiomContext ax = TestSupport.newContext();
        MoneyAmount result = Abs.abs(ax, AbsInput.newBuilder().setAmount(money("-4.50", "USD")).build());
        assertEquals("", result.getError().getCode());
        assertEquals("4.50", result.getAmount());
    }

    @Test
    public void positiveStaysPositive() {
        AxiomContext ax = TestSupport.newContext();
        MoneyAmount result = Abs.abs(ax, AbsInput.newBuilder().setAmount(money("4.50", "USD")).build());
        assertEquals("4.50", result.getAmount());
    }

    @Test
    public void zeroStaysZero() {
        AxiomContext ax = TestSupport.newContext();
        MoneyAmount result = Abs.abs(ax, AbsInput.newBuilder().setAmount(money("0.00", "USD")).build());
        assertEquals("0.00", result.getAmount());
    }
}
