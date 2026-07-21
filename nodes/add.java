package nodes;

import axiom.AxiomContext;
import gen.Messages.AddInput;
import gen.Messages.MoneyAmount;

import javax.money.CurrencyUnit;
import java.math.BigDecimal;
import java.util.Map;

public class Add {

    /**
     * Add two same-currency amounts exactly: a + b. Currency mismatch
     * returns CURRENCY_MISMATCH rather than crashing.
     */
    public static MoneyAmount add(AxiomContext ax, AddInput input) {
        ax.log().info("add handling", Map.of());
        try {
            MoneyUtil.Parsed a = MoneyUtil.parse(input.getA(), "a");
            MoneyUtil.Parsed b = MoneyUtil.parse(input.getB(), "b");
            MoneyUtil.requireSameCurrency(a.currency, b.currency);
            BigDecimal sum = a.amount.add(b.amount);
            return MoneyUtil.build(sum, a.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
