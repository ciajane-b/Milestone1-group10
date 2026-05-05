/**
 * ReservationPayment — concrete implementation of PaymentFramework.
 *
 * Applies a rank-based military discount on top of the standard 12% VAT
 * that the abstract parent already handles. Discount tiers:
 *   CPT / LT  → 20 %  (officer quarters)
 *   SGT / SSG → 15 %  (NCO quarters)
 *   SPC / CPL → 10 %  (specialist quarters)
 *   PVT / PFC →  5 %  (enlisted / barracks)
 *   (default) →  0 %
 *
 * Usage:
 *   PaymentFramework pf = new ReservationPayment(balance, chargeAmount, rank);
 *   pf.processInvoice();
 */
public class ReservationPayment extends PaymentFramework {

    private String rank;

    public ReservationPayment(double balance, double amount, String rank) {
        super(balance, amount);
        this.rank = rank;
    }

    /**
     * Returns the current balance after the transaction so callers can
     * persist or display it.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Returns the discount rate (0.0 – 1.0) for the given rank.
     */
    public static double getDiscountRate(String rank) {
        switch (rank) {
            case "CPT": case "LT":  return 0.20;
            case "SGT": case "SSG": return 0.15;
            case "SPC": case "CPL": return 0.10;
            case "PVT": case "PFC": return 0.05;
            default:                return 0.00;
        }
    }

    /**
     * Step 3 of the PaymentFramework template method.
     * Applies a rank-based military discount to the VAT-inclusive amount.
     */
    @Override
    protected double applyDiscount(double amount) {
        double rate = getDiscountRate(rank);
        double discount = amount * rate;
        if (rate > 0) {
            System.out.printf("|  |    Rank-based discount (%s – %.0f%%): -PHP %.2f%n",
                    rank, rate * 100, discount);
        } else {
            System.out.println("|  |    No rank-based discount applicable.");
        }
        return amount - discount;
    }
}
