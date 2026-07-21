package nodes;

import axiom.AxiomContext;
import gen.Messages.SubtractInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubtractTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void subtractsTwoAmounts_handComputed() {
        AxiomContext ax = TestSupport.newContext();
        SubtractInput input = SubtractInput.newBuilder()
            .setA(money("10.00", "USD"))
            .setB(money("3.25", "USD"))
            .build();
        MoneyAmount result = Subtract.subtract(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("6.75", result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    public void resultCanGoNegative() {
        AxiomContext ax = TestSupport.newContext();
        SubtractInput input = SubtractInput.newBuilder()
            .setA(money("3.00", "USD"))
            .setB(money("10.00", "USD"))
            .build();
        MoneyAmount result = Subtract.subtract(ax, input);
        assertEquals("-7.00", result.getAmount());
    }

    @Test
    public void currencyMismatch_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        SubtractInput input = SubtractInput.newBuilder()
            .setA(money("10.00", "USD"))
            .setB(money("1.00", "JPY"))
            .build();
        MoneyAmount result = Subtract.subtract(ax, input);
        assertEquals("CURRENCY_MISMATCH", result.getError().getCode());
    }
}
