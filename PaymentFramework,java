abstract class PaymentFramework {

    protected double balance;
    protected double amount;

    public PaymentFramework(double balance, double amount) {
        this.balance = balance;
        this.amount = amount;
    }

    // Step 1: Validate payment
    protected boolean validatePayment() {
        if (balance >= amount) {
            System.out.println("Payment validated.");
            return true;
        } else {
            System.out.println("Insufficient balance.");
            return false;
        }
    }

    // Step 2: Apply 12% VAT
    protected double applyVAT(double amount) {
        double vat = amount * 0.12;
        return amount + vat;
    }

    // Step 3: Apply discount (to be implemented by subclass)
    protected abstract double applyDiscount(double amount);

    // Step 4: Finalize transaction
    protected void finalizeTransaction(double finalAmount) {
        balance -= finalAmount;
        System.out.println("Transaction completed.");
        System.out.println("Remaining balance: " + balance);
    }

    // Step 5: Concrete method (can be overridden)
    public void processInvoice() {
        if (validatePayment()) {
            double totalWithVAT = applyVAT(amount);
            double discountedAmount = applyDiscount(totalWithVAT);
            finalizeTransaction(discountedAmount);

            if (discountedAmount > 0) {
                System.out.println("Invoice processed successfully.");
            } else {
                System.out.println("Invoice failed.");
            }
        } else {
            System.out.println("Invoice cannot be processed.");
        }
    }
}
