package nodes;

import gen.Messages.MoneyAmount;
import gen.Messages.MoneyError;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.UnknownCurrencyException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Shared boilerplate for every node in this package: parse/validate a
 * {@link MoneyAmount} envelope into exact {@link BigDecimal} + validated
 * {@link CurrencyUnit}, build structured errors, and the Allocate algorithm.
 *
 * <p>Design note: all ARITHMETIC in this package is done with plain
 * {@code java.math.BigDecimal} (the JDK's own arbitrary-precision exact
 * decimal type), not by chaining {@code org.javamoney.moneta.Money}'s own
 * operator methods. This was a deliberate choice after verifying empirically
 * that re-extracting a {@code BigDecimal} from a {@code Money} value after
 * {@code Money.with(Monetary.getRounding(...))} does NOT reliably preserve
 * the requested scale (e.g. rounding "2.005" to scale 2 HALF_EVEN yields a
 * {@code BigDecimal} of scale 0 ("2"), not scale 2 ("2.00")) — Moneta's
 * default {@code MonetaryContext} is a 256-significant-digit MathContext,
 * not a fixed decimal scale, so it silently strips trailing zeros instead of
 * honoring the target scale. Since every op in this package must emit an
 * amount string at an EXACT, caller-predictable scale, we control scale
 * ourselves via {@code BigDecimal.setScale}/{@code divide(scale, mode)} and
 * use Moneta/JSR-354 ({@code javax.money.Monetary}) only for what it
 * genuinely owns well: ISO 4217 currency validation and metadata (default
 * fraction digits, numeric code) — the JSR-354 reference implementation's
 * real "hard part," a currency catalog we should not reimplement.
 */
final class MoneyUtil {
    private MoneyUtil() {}

    /** An explicit target scale may not exceed this many decimal places. */
    static final int MAX_SCALE = 100;

    // Plain decimal only: optional leading '-', digits, optional '.', digits.
    // No scientific notation, no leading '+', no grouping separators — this
    // package's amounts are exact and canonical, never locale-formatted.
    private static final Pattern PLAIN_DECIMAL = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    /** Thrown internally to unwind straight to a structured MoneyError. */
    static final class MoneyOpException extends RuntimeException {
        final String code;
        MoneyOpException(String code, String message) {
            super(message);
            this.code = code;
        }
    }

    static MoneyOpException invalid(String message) {
        return new MoneyOpException("INVALID_ARGUMENT", message);
    }

    static MoneyError toError(MoneyOpException e) {
        return MoneyError.newBuilder().setCode(e.code).setMessage(e.getMessage()).build();
    }

    static MoneyError toError(String code, String message) {
        return MoneyError.newBuilder().setCode(code).setMessage(message).build();
    }

    static MoneyAmount amountWithError(MoneyOpException e) {
        return MoneyAmount.newBuilder().setError(toError(e)).build();
    }

    /** Resolve+validate an ISO 4217 currency code, case-insensitively. */
    static CurrencyUnit currency(String code) {
        if (code == null || code.isBlank()) {
            throw invalid("currency is required");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        try {
            return Monetary.getCurrency(normalized);
        } catch (UnknownCurrencyException | IllegalArgumentException e) {
            throw new MoneyOpException("UNKNOWN_CURRENCY",
                "unrecognized ISO 4217 currency code: " + code);
        }
    }

    /** Parse a plain, exact decimal string field into a BigDecimal. */
    static BigDecimal decimal(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw invalid(fieldName + " is required");
        }
        String trimmed = raw.trim();
        if (!PLAIN_DECIMAL.matcher(trimmed).matches()) {
            throw invalid(fieldName + " must be a plain decimal string (no scientific notation, "
                + "no leading '+', no separators), got: " + raw);
        }
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            throw invalid(fieldName + " is not a valid decimal string: " + raw);
        }
    }

    /** A validated (amount, currency) pair extracted from a MoneyAmount envelope. */
    static final class Parsed {
        final BigDecimal amount;
        final CurrencyUnit currency;
        Parsed(BigDecimal amount, CurrencyUnit currency) {
            this.amount = amount;
            this.currency = currency;
        }
    }

    static Parsed parse(MoneyAmount ma, String fieldName) {
        if (ma == null) {
            throw invalid(fieldName + " is required");
        }
        BigDecimal amount = decimal(ma.getAmount(), fieldName + ".amount");
        CurrencyUnit cu = currency(ma.getCurrency());
        return new Parsed(amount, cu);
    }

    static void requireSameCurrency(CurrencyUnit a, CurrencyUnit b) {
        if (!a.equals(b)) {
            throw new MoneyOpException("CURRENCY_MISMATCH",
                "currency mismatch: " + a.getCurrencyCode() + " vs " + b.getCurrencyCode());
        }
    }

    static MoneyAmount build(BigDecimal amount, CurrencyUnit currency) {
        return MoneyAmount.newBuilder()
            .setAmount(amount.toPlainString())
            .setCurrency(currency.getCurrencyCode())
            .build();
    }

    static RoundingMode roundingMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return RoundingMode.HALF_UP;
        }
        try {
            return RoundingMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new MoneyOpException("INVALID_ROUNDING_MODE",
                "unrecognized rounding mode (expected one of HALF_UP, HALF_DOWN, HALF_EVEN, "
                + "UP, DOWN, CEILING, FLOOR): " + raw);
        }
    }

    /**
     * Resolve an explicit-or-default target scale, validating the bound.
     * {@code present} must come from the proto3 {@code optional} field's own
     * {@code has*()} presence check — never inferred from a sentinel value,
     * since 0 is itself a valid, meaningful requested scale (e.g. JPY).
     */
    static int resolveScale(boolean present, int requested, int defaultScale) {
        int scale = present ? requested : defaultScale;
        if (scale < 0) {
            throw invalid("scale must not be negative: " + scale);
        }
        if (scale > MAX_SCALE) {
            throw invalid("scale exceeds the maximum of " + MAX_SCALE + ": " + scale);
        }
        return scale;
    }

    /** The JDK's Locale.US symbol for a currency code — deterministic (never the caller's
     *  ambient default locale), NOT the caller's locale. For a truly locale-aware symbol,
     *  use christiangeorgelucas/locale-tools' GetCurrencyInfo instead. */
    static String symbolFor(String isoCode) {
        try {
            return Currency.getInstance(isoCode).getSymbol(Locale.US);
        } catch (IllegalArgumentException e) {
            return isoCode;
        }
    }

    /**
     * Split the unscaled integer value of {@code total} (which must be
     * non-negative) across {@code ratios} (every entry must be a positive
     * integer) using the largest-remainder method: each part first gets the
     * integer floor of its proportional share, then the leftover minor
     * units (0..ratios.length-1 of them) are distributed one at a time to
     * the parts with the largest fractional remainder, ties broken by
     * ascending index. This guarantees sum(parts) == total exactly.
     */
    static BigInteger[] allocateUnscaled(BigInteger total, long[] ratios) {
        if (ratios == null || ratios.length == 0) {
            throw invalid("ratios must not be empty");
        }
        if (total.signum() < 0) {
            throw invalid("amount must not be negative — Allocate does not support negative "
                + "amounts (the remainder-distribution convention is ambiguous for debts); "
                + "negate the parts of a positive allocation instead");
        }
        long ratioSum = 0;
        for (long r : ratios) {
            if (r <= 0) {
                throw invalid("every ratio must be a positive integer, got: " + r);
            }
            ratioSum += r;
        }
        BigInteger ratioSumBI = BigInteger.valueOf(ratioSum);
        int n = ratios.length;
        BigInteger[] floors = new BigInteger[n];
        BigInteger[] remainders = new BigInteger[n];
        BigInteger allocated = BigInteger.ZERO;
        for (int i = 0; i < n; i++) {
            BigInteger num = total.multiply(BigInteger.valueOf(ratios[i]));
            BigInteger[] dr = num.divideAndRemainder(ratioSumBI);
            floors[i] = dr[0];
            remainders[i] = dr[1];
            allocated = allocated.add(floors[i]);
        }
        BigInteger leftover = total.subtract(allocated);
        // leftover is guaranteed 0 <= leftover < n by the math above.
        int leftoverInt = leftover.intValueExact();

        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (x, y) -> {
            int cmp = remainders[y].compareTo(remainders[x]); // descending remainder
            return cmp != 0 ? cmp : Integer.compare(x, y);    // ascending index tiebreak
        });
        for (int k = 0; k < leftoverInt; k++) {
            int idx = order[k];
            floors[idx] = floors[idx].add(BigInteger.ONE);
        }
        return floors;
    }
}
