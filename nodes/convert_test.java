package nodes;

import axiom.AxiomContext;
import gen.Messages.ConvertInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConvertTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void convertsAtCallerSuppliedRate_handComputed() {
        // 10.00 USD * 0.92 = 9.20 EUR exactly.
        AxiomContext ax = TestSupport.newContext();
        ConvertInput input = ConvertInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setTargetCurrency("EUR")
            .setRate("0.92")
            .build();
        MoneyAmount result = Convert.convert(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("9.20", result.getAmount());
        assertEquals("EUR", result.getCurrency());
    }

    @Test
    public void roundsToTargetCurrencyDefaultScale_jpyIsZero() {
        // 10.00 USD * 150.345 = 1503.45 raw, rounded HALF_UP to JPY's 0
        // fraction digits -> 1503.
        AxiomContext ax = TestSupport.newContext();
        ConvertInput input = ConvertInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setTargetCurrency("JPY")
            .setRate("150.345")
            .build();
        MoneyAmount result = Convert.convert(ax, input);
        assertEquals("1503", result.getAmount());
        assertEquals("JPY", result.getCurrency());
    }

    @Test
    public void nonPositiveRate_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        ConvertInput input = ConvertInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setTargetCurrency("EUR")
            .setRate("0")
            .build();
        MoneyAmount result = Convert.convert(ax, input);
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }

    @Test
    public void unknownTargetCurrency_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        ConvertInput input = ConvertInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setTargetCurrency("ZZZZZ")
            .setRate("1")
            .build();
        MoneyAmount result = Convert.convert(ax, input);
        assertEquals("UNKNOWN_CURRENCY", result.getError().getCode());
    }
}
