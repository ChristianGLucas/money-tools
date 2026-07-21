package nodes;

import axiom.AxiomContext;
import gen.Messages.AddInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AddTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void addsTwoAmounts_handComputed() {
        AxiomContext ax = TestSupport.newContext();
        AddInput input = AddInput.newBuilder()
            .setA(money("10.00", "USD"))
            .setB(money("5.50", "USD"))
            .build();
        MoneyAmount result = Add.add(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("15.50", result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    public void independentOracle_pointOneplusPointTwoIsExactPointThree() {
        // The canonical binary-floating-point failure case: 0.1 + 0.2 != 0.3
        // under IEEE 754 double, but IS exactly 0.3 under exact decimal
        // arithmetic — the entire reason this package parses amounts as
        // strings into BigDecimal instead of double.
        AxiomContext ax = TestSupport.newContext();
        AddInput input = AddInput.newBuilder()
            .setA(money("0.1", "USD"))
            .setB(money("0.2", "USD"))
            .build();
        MoneyAmount result = Add.add(ax, input);
        assertEquals("0.3", result.getAmount(), "0.1 + 0.2 must be exactly \"0.3\", not the "
            + "0.30000000000000004 double-precision approximation");
    }

    @Test
    public void currencyMismatch_returnsStructuredError_notCrash() {
        AxiomContext ax = TestSupport.newContext();
        AddInput input = AddInput.newBuilder()
            .setA(money("10.00", "USD"))
            .setB(money("10.00", "EUR"))
            .build();
        MoneyAmount result = Add.add(ax, input);
        assertEquals("CURRENCY_MISMATCH", result.getError().getCode());
        assertFalse(result.getError().getMessage().isEmpty());
    }

    @Test
    public void unknownCurrency_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        AddInput input = AddInput.newBuilder()
            .setA(money("10.00", "ZZZZZ"))
            .setB(money("1.00", "ZZZZZ"))
            .build();
        MoneyAmount result = Add.add(ax, input);
        assertEquals("UNKNOWN_CURRENCY", result.getError().getCode());
    }

    @Test
    public void malformedAmount_rejectsScientificNotation_notCrash() {
        AxiomContext ax = TestSupport.newContext();
        AddInput input = AddInput.newBuilder()
            .setA(money("1E3", "USD"))
            .setB(money("1.00", "USD"))
            .build();
        MoneyAmount result = Add.add(ax, input);
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }
}
