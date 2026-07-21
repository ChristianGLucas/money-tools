package nodes;

import axiom.AxiomContext;
import gen.Messages.SubtractInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.util.Map;

public class Subtract {

    /**
     * Subtract two same-currency amounts exactly: a - b. Currency mismatch
     * returns CURRENCY_MISMATCH rather than crashing.
     */
    public static MoneyAmount subtract(AxiomContext ax, SubtractInput input) {
        ax.log().info("subtract handling", Map.of());
        try {
            MoneyUtil.Parsed a = MoneyUtil.parse(input.getA(), "a");
            MoneyUtil.Parsed b = MoneyUtil.parse(input.getB(), "b");
            MoneyUtil.requireSameCurrency(a.currency, b.currency);
            BigDecimal diff = a.amount.subtract(b.amount);
            return MoneyUtil.build(diff, a.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
