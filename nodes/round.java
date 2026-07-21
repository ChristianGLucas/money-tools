package nodes;

import axiom.AxiomContext;
import gen.Messages.RoundInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class Round {

    /**
     * Round an amount to `scale` decimal places (default: the amount's
     * currency's own default fraction digits, e.g. 2 for USD, 0 for JPY)
     * using `rounding_mode` (default HALF_UP).
     */
    public static MoneyAmount round(AxiomContext ax, RoundInput input) {
        ax.log().info("round handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            int scale = MoneyUtil.resolveScale(input.hasScale(), input.getScale(),
                amount.currency.getDefaultFractionDigits());
            RoundingMode mode = MoneyUtil.roundingMode(input.getRoundingMode());
            BigDecimal rounded = amount.amount.setScale(scale, mode);
            return MoneyUtil.build(rounded, amount.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
