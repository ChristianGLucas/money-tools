package nodes;

import axiom.AxiomContext;
import gen.Messages.ApplyPercentageInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApplyPercentageTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void tenPercentOfTwoHundred_handComputed() {
        // 200.00 (scale 2) * 10 (scale 0) = 2000.00 (scale 2);
        // movePointLeft(2) -> 20.0000 (scale 4). Exact decimal-shift
        // arithmetic — independently verifiable by hand.
        AxiomContext ax = TestSupport.newContext();
        ApplyPercentageInput input = ApplyPercentageInput.newBuilder()
            .setAmount(money("200.00", "USD"))
            .setPercent("10")
            .build();
        MoneyAmount result = ApplyPercentage.applyPercentage(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("20.0000", result.getAmount());
    }

    @Test
    public void fractionalPercent() {
        // 200.00 * 2.5 = 500.000, movePointLeft(2) -> 5.00000.
        AxiomContext ax = TestSupport.newContext();
        ApplyPercentageInput input = ApplyPercentageInput.newBuilder()
            .setAmount(money("200.00", "USD"))
            .setPercent("2.5")
            .build();
        MoneyAmount result = ApplyPercentage.applyPercentage(ax, input);
        assertEquals("5.00000", result.getAmount());
    }

    @Test
    public void negativePercent_discount() {
        AxiomContext ax = TestSupport.newContext();
        ApplyPercentageInput input = ApplyPercentageInput.newBuilder()
            .setAmount(money("100.00", "USD"))
            .setPercent("-15")
            .build();
        MoneyAmount result = ApplyPercentage.applyPercentage(ax, input);
        assertEquals("-15.0000", result.getAmount());
    }
}
