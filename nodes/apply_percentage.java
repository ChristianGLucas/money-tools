package nodes;

import axiom.AxiomContext;
import gen.Messages.ApplyPercentageInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.util.Map;

public class ApplyPercentage {

    /**
     * Apply a percentage to an amount: amount * (percent / 100), e.g.
     * percent="10" on 200.00 USD -> "20.0000" USD. Exact — dividing by 100
     * is always an exact terminating decimal shift, never a rounded
     * division — but the scale GROWS by +2 rather than normalizing back to
     * the input's own scale; pair with Round for a specific target scale.
     */
    public static MoneyAmount applyPercentage(AxiomContext ax, ApplyPercentageInput input) {
        ax.log().info("applyPercentage handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            BigDecimal percent = MoneyUtil.decimal(input.getPercent(), "percent");
            BigDecimal result = amount.amount.multiply(percent).movePointLeft(2);
            return MoneyUtil.build(result, amount.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
