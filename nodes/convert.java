package nodes;

import axiom.AxiomContext;
import gen.Messages.ConvertInput;
import gen.Messages.MoneyAmount;

import javax.money.CurrencyUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class Convert {

    /**
     * Convert an amount into another currency using a CALLER-SUPPLIED
     * exchange rate: result = amount * rate, rounded to `scale` decimal
     * places (default: target_currency's own default fraction digits)
     * using `rounding_mode` (default HALF_UP). This node never fetches a
     * live rate over the network — the rate is always an explicit input.
     */
    public static MoneyAmount convert(AxiomContext ax, ConvertInput input) {
        ax.log().info("convert handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            CurrencyUnit target = MoneyUtil.currency(input.getTargetCurrency());
            BigDecimal rate = MoneyUtil.decimal(input.getRate(), "rate");
            if (rate.signum() <= 0) {
                throw MoneyUtil.invalid("rate must be positive, got: " + input.getRate());
            }
            int scale = MoneyUtil.resolveScale(input.hasScale(), input.getScale(),
                target.getDefaultFractionDigits());
            RoundingMode mode = MoneyUtil.roundingMode(input.getRoundingMode());
            BigDecimal raw = amount.amount.multiply(rate);
            BigDecimal result = raw.setScale(scale, mode);
            return MoneyUtil.build(result, target);
        } catch (MoneyUtil.MoneyOpException e) {
            return MoneyUtil.amountWithError(e);
        }
    }
}
