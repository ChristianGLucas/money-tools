package nodes;

import axiom.AxiomContext;
import gen.Messages.SumInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.util.Map;

public class Sum {

    /**
     * Total a list of same-currency amounts exactly. An empty list, or a
     * list mixing currencies, returns a structured error rather than an
     * arbitrary zero/first-currency guess.
     */
    public static MoneyAmount sum(AxiomContext ax, SumInput input) {
        ax.log().info("sum handling", Map.of());
        try {
            if (input.getAmountsCount() == 0) {
                throw MoneyUtil.invalid("amounts must not be empty");
            }
            if (input.getAmountsCount() > MoneyUtil.MAX_LIST_SIZE) {
                throw MoneyUtil.invalid("amounts exceeds the maximum of " + MoneyUtil.MAX_LIST_SIZE + " entries");
            }
            MoneyUtil.Parsed first = MoneyUtil.parse(input.getAmounts(0), "amounts[0]");
            BigDecimal total = first.amount;
            for (int i = 1; i < input.getAmountsCount(); i++) {
                MoneyUtil.Parsed next = MoneyUtil.parse(input.getAmounts(i), "amounts[" + i + "]");
                MoneyUtil.requireSameCurrency(first.currency, next.currency);
                total = total.add(next.amount);
            }
            return MoneyUtil.build(total, first.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
