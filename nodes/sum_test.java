package nodes;

import axiom.AxiomContext;
import gen.Messages.SumInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SumTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void totalsAListOfAmounts_handComputed() {
        AxiomContext ax = TestSupport.newContext();
        SumInput input = SumInput.newBuilder()
            .addAmounts(money("10.00", "USD"))
            .addAmounts(money("20.00", "USD"))
            .addAmounts(money("30.00", "USD"))
            .build();
        MoneyAmount result = Sum.sum(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("60.00", result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    public void emptyList_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        SumInput input = SumInput.newBuilder().build();
        MoneyAmount result = Sum.sum(ax, input);
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }

    @Test
    public void mixedCurrencies_returnsStructuredError_notSilentCoercion() {
        AxiomContext ax = TestSupport.newContext();
        SumInput input = SumInput.newBuilder()
            .addAmounts(money("10.00", "USD"))
            .addAmounts(money("10.00", "EUR"))
            .build();
        MoneyAmount result = Sum.sum(ax, input);
        assertEquals("CURRENCY_MISMATCH", result.getError().getCode());
    }
}
