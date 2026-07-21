package nodes;

import axiom.AxiomContext;
import gen.Messages.DivideInput;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DivideTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void defaultScaleUsesCurrencyFractionDigits_handComputed() {
        // 10.00 / 3 = 3.3333... rounded HALF_UP to USD's default 2 places -> 3.33.
        AxiomContext ax = TestSupport.newContext();
        DivideInput input = DivideInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setDivisor("3")
            .build();
        MoneyAmount result = Divide.divide(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals("3.33", result.getAmount());
    }

    @Test
    public void explicitScaleOverridesCurrencyDefault() {
        AxiomContext ax = TestSupport.newContext();
        DivideInput input = DivideInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setDivisor("3")
            .setScale(4)
            .build();
        MoneyAmount result = Divide.divide(ax, input);
        assertEquals("3.3333", result.getAmount());
    }

    @Test
    public void zeroFractionDigitCurrency_usesItsOwnDefaultScale() {
        // 100 JPY / 3 = 33.333..., JPY's default fraction digits is 0.
        AxiomContext ax = TestSupport.newContext();
        DivideInput input = DivideInput.newBuilder()
            .setAmount(money("100", "JPY"))
            .setDivisor("3")
            .build();
        MoneyAmount result = Divide.divide(ax, input);
        assertEquals("33", result.getAmount());
    }

    @Test
    public void divisionByZero_returnsStructuredError_notCrash() {
        AxiomContext ax = TestSupport.newContext();
        DivideInput input = DivideInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setDivisor("0")
            .build();
        MoneyAmount result = Divide.divide(ax, input);
        assertEquals("DIVISION_BY_ZERO", result.getError().getCode());
    }

    @Test
    public void invalidRoundingMode_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        DivideInput input = DivideInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .setDivisor("3")
            .setRoundingMode("NOT_A_MODE")
            .build();
        MoneyAmount result = Divide.divide(ax, input);
        assertEquals("INVALID_ROUNDING_MODE", result.getError().getCode());
    }
}
