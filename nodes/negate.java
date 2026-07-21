package nodes;

import axiom.AxiomContext;
import gen.Messages.NegateInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.util.Map;

public class Negate {

    /** Flip the sign of an amount: 4.50 USD -> -4.50 USD and back. */
    public static MoneyAmount negate(AxiomContext ax, NegateInput input) {
        ax.log().info("negate handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            BigDecimal negated = amount.amount.negate();
            return MoneyUtil.build(negated, amount.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
