# money-tools

Composable Axiom exact monetary-arithmetic nodes, built for the Axiom marketplace.

Wraps [org.javamoney/Moneta](https://github.com/JavaMoney/jsr354-ri) (Apache-2.0,
the JSR-354 reference implementation) for ISO 4217 currency validation and
metadata, and `java.math.BigDecimal` (the JDK's own arbitrary-precision exact
decimal type) for all arithmetic. Every amount is a plain decimal **string**
end-to-end — never a `float`/`double` — because money in floating point is a
correctness bug, not a rounding nicety.

This package is exact monetary **arithmetic and allocation** — add, subtract,
multiply, divide, remainder-safe allocation across ratios, currency-scale
rounding, comparison, sign classification, negate/abs, percentage application,
caller-supplied-rate conversion, and ISO 4217 currency metadata. It is
deliberately **not** locale/CLDR display formatting (no thousands separators,
no localized symbol placement) — pair it with
[`christiangeorgelucas/locale-tools`](https://github.com/ChristianGLucas/locale-tools)
for that.

## Nodes

| Node | What it does |
|---|---|
| `Add` | `a + b` for two same-currency amounts |
| `Subtract` | `a - b` for two same-currency amounts |
| `Multiply` | amount × an exact decimal scalar factor |
| `Divide` | amount ÷ an exact decimal scalar divisor, at a target scale + rounding mode |
| `Sum` | total a list of same-currency amounts |
| `Allocate` | split an amount across integer ratios with no rounding loss — the classic "split $0.05 three ways" problem, solved deterministically (largest-remainder method) |
| `Round` | round to a target scale (default: the currency's own default fraction digits) at a chosen rounding mode |
| `Compare` | order two same-currency amounts by numeric value |
| `Sign` | classify an amount's sign (zero / positive / negative) |
| `Convert` | apply a **caller-supplied** exchange rate to move into another currency — never a live network fetch |
| `Negate` | flip an amount's sign |
| `Abs` | the absolute value of an amount |
| `ApplyPercentage` | amount × (percent / 100), exactly |
| `GetCurrencyMetadata` | ISO 4217 default fraction digits, numeric code, and a fixed-reference-locale symbol |

## License

MIT. See [LICENSE](LICENSE).

Third-party: [org.javamoney/Moneta](https://github.com/JavaMoney/jsr354-ri)
(Apache-2.0) and its transitive dependency tree (all Apache-2.0, with one
dual-licensed EPL-2.0/GPL-2.0-with-Classpath-Exception annotations-only jar,
`jakarta.annotation-api`).
