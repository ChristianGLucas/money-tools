package nodes;

import axiom.AxiomContext;
import gen.Messages.RoundInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoundTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void defaultHalfUp_handComputed() {
        // 2.005 is exactly halfway between 2.00 and 2.01; HALF_UP rounds
        // away from zero -> 2.01.
        AxiomContext ax = TestSupport.newContext();
        RoundInput input = RoundInput.newBuilder()
            .setAmount(money("2.005", "USD"))
            .build();
        MoneyAmount result = Round.round(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("2.01", result.getAmount());
    }

    @Test
    public void halfEven_bankersRounding_independentOracle() {
        // 2.005 is exactly halfway between 2.00 (even last digit) and 2.01
        // (odd) -> HALF_EVEN picks the even neighbor, 2.00. 2.015 is exactly
        // halfway between 2.01 (odd) and 2.02 (even) -> picks 2.02. These
        // are the standard, independently-documented "banker's rounding"
        // outcomes for exactly-halfway decimal values.
        AxiomContext ax = TestSupport.newContext();
        MoneyAmount r1 = Round.round(ax, RoundInput.newBuilder()
            .setAmount(money("2.005", "USD")).setRoundingMode("HALF_EVEN").build());
        assertEquals("2.00", r1.getAmount());

        MoneyAmount r2 = Round.round(ax, RoundInput.newBuilder()
            .setAmount(money("2.015", "USD")).setRoundingMode("HALF_EVEN").build());
        assertEquals("2.02", r2.getAmount());
    }

    @Test
    public void defaultScaleUsesCurrencyFractionDigits_jpyIsZero() {
        AxiomContext ax = TestSupport.newContext();
        RoundInput input = RoundInput.newBuilder()
            .setAmount(money("1500.6", "JPY"))
            .build();
        MoneyAmount result = Round.round(ax, input);
        assertEquals("1501", result.getAmount());
    }

    @Test
    public void explicitZeroScale_isRespected_notTreatedAsUnset() {
        // Regression guard: scale is a proto3 `optional int32` specifically
        // so an explicit 0 is distinguishable from "not set."
        AxiomContext ax = TestSupport.newContext();
        RoundInput input = RoundInput.newBuilder()
            .setAmount(money("19.99", "USD"))
            .setScale(0)
            .build();
        MoneyAmount result = Round.round(ax, input);
        assertEquals("20", result.getAmount());
    }
}
