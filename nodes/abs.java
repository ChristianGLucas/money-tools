package nodes;

import axiom.AxiomContext;
import gen.Messages.AbsInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.util.Map;

public class Abs {

    /** The absolute value of an amount: -4.50 USD -> 4.50 USD. */
    public static MoneyAmount abs(AxiomContext ax, AbsInput input) {
        ax.log().info("abs handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            BigDecimal absolute = amount.amount.abs();
            return MoneyUtil.build(absolute, amount.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
