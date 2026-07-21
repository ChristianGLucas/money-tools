package nodes;

import axiom.AxiomContext;
import gen.Messages.DivideInput;
import gen.Messages.MoneyAmount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class Divide {

    /**
     * Divide an amount by an exact decimal scalar divisor: amount / divisor,
     * rounded to `scale` decimal places (default: the amount's currency's
     * own default fraction digits) using `rounding_mode` (default HALF_UP).
     * Division can be non-terminating (e.g. 10/3), so a target scale and
     * rounding mode are always applied — never a bare unrounded divide.
     */
    public static MoneyAmount divide(AxiomContext ax, DivideInput input) {
        ax.log().info("divide handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            BigDecimal divisor = MoneyUtil.decimal(input.getDivisor(), "divisor");
            if (divisor.signum() == 0) {
                throw new MoneyUtil.MoneyOpException("DIVISION_BY_ZERO", "divisor must not be zero");
            }
            int scale = MoneyUtil.resolveScale(input.hasScale(), input.getScale(),
                amount.currency.getDefaultFractionDigits());
            RoundingMode mode = MoneyUtil.roundingMode(input.getRoundingMode());
            BigDecimal result = amount.amount.divide(divisor, scale, mode);
            return MoneyUtil.build(result, amount.currency);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
