package nodes;

import axiom.AxiomContext;
import gen.Messages.MultiplyInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.util.Map;

public class Multiply {

    /**
     * Multiply an amount by an exact decimal scalar factor: amount * factor.
     * The result keeps full exact precision (scale grows to
     * amount.scale + factor.scale) — pair with Round to land on a
     * currency's conventional scale.
     */
    public static MoneyAmount multiply(AxiomContext ax, MultiplyInput input) {
        ax.log().info("multiply handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            BigDecimal factor = MoneyUtil.decimal(input.getFactor(), "factor");
            BigDecimal product = amount.amount.multiply(factor);
            return MoneyUtil.build(product, amount.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
