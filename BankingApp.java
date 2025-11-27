import java.util.*;

public class BankingApp {


    static class Customer {
        private final String name;
        private final String email;

        public Customer(String name, String email) {
            this.name = name;
            this.email = email;
        }
        public String getName() { return name; }
    }

    static class Account {
        private final String accountNumber;
        private final Customer customer;
        private final String type;
        private double balance = 0;
        private final List<String> transactions = new ArrayList<>();

        public Account(String accountNumber, Customer customer, String type) {
            this.accountNumber = accountNumber;
            this.customer = customer;
            this.type = type;
        }

        public String getAccountNumber() { return accountNumber; }
        public Customer getCustomer() { return customer; }
        public double getBalance() { return balance; }
        public List<String> getTransactions() { return transactions; }

        public void deposit(double amount) {
            balance += amount;
            record("DEPOSIT", amount);
        }

        public void withdraw(double amount) throws Exception {
            if (balance < amount) throw new Exception("Insufficient funds.");
            balance -= amount;
            record("WITHDRAW", -amount);
        }

        public void transfer(Account to, double amount) throws Exception {
            withdraw(amount);
            to.deposit(amount);
            record("TRANSFER_TO " + to.accountNumber, -amount);
            to.record("TRANSFER_FROM " + accountNumber, amount);
        }

        private void record(String type, double amount) {
            transactions.add(String.format("%s (%.2f) | Balance: %.2f", type, amount, balance));
        }
    }


    static class AccountRepository {
        private final Map<String, Account> accounts = new HashMap<>();
        private int id = 1;

        public Account create(Customer customer, String type) {
            String accNo = "ACC" + id++;
            Account acc = new Account(accNo, customer, type);
            accounts.put(accNo, acc);
            return acc;
        }

        public Account find(String accNo) {
            return accounts.get(accNo);
        }

        public List<Account> findAll() {
            return new ArrayList<>(accounts.values());
        }

        public List<Account> findByName(String name) {
            List<Account> result = new ArrayList<>();
            for (Account acc : accounts.values()) {
                if (acc.getCustomer().getName().equalsIgnoreCase(name)) {
                    result.add(acc);
                }
            }
            return result;
        }
    }


    static class BankingService {
        private final AccountRepository repo = new AccountRepository();

        public Account openAccount(String name, String email, String type) throws Exception {
            validate(name, email, type);
            return repo.create(new Customer(name, email), type);
        }

        public void deposit(String acc, double amt) throws Exception {
            validateAmount(amt);
            get(acc).deposit(amt);
        }

        public void withdraw(String acc, double amt) throws Exception {
            validateAmount(amt);
            get(acc).withdraw(amt);
        }

        public void transfer(String from, String to, double amt) throws Exception {
            validateAmount(amt);
            get(from).transfer(get(to), amt);
        }

        public List<String> getStatement(String acc) throws Exception {
            return get(acc).getTransactions();
        }

        public List<Account> listAll() {
            return repo.findAll();
        }

        public List<Account> search(String name) {
            return repo.findByName(name);
        }

        private Account get(String accNo) throws Exception {
            Account acc = repo.find(accNo);
            if (acc == null) throw new Exception("Account not found.");
            return acc;
        }

        private void validate(String name, String email, String type) throws Exception {
            if (name.isBlank()) throw new Exception("Invalid name.");
            if (!email.contains("@")) throw new Exception("Invalid email.");
            if (!type.equalsIgnoreCase("SAVINGS") && !type.equalsIgnoreCase("CURRENT"))
                throw new Exception("Type must be SAVINGS or CURRENT.");
        }

        private void validateAmount(double amt) throws Exception {
            if (amt <= 0) throw new Exception("Amount must be > 0");
        }
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankingService bank = new BankingService();

        while (true) {
            System.out.println("""
                ==== Banking Menu ====
                1. Open Account
                2. Deposit
                3. Withdraw
                4. Transfer
                5. Statement
                6. List Accounts
                7. Search Accounts
                8. Exit
                Enter choice: """);

            int ch = Integer.parseInt(sc.nextLine());

            try {
                switch (ch) {
                    case 1 -> {
                        System.out.print("Name: "); String n = sc.nextLine();
                        System.out.print("Email: "); String e = sc.nextLine();
                        System.out.print("Type: "); String t = sc.nextLine();
                        Account a = bank.openAccount(n, e, t);
                        System.out.println("Account Created: " + a.getAccountNumber());
                    }
                    case 2 -> {
                        System.out.print("Account No: "); String acc = sc.nextLine();
                        System.out.print("Amount: "); double amt = Double.parseDouble(sc.nextLine());
                        bank.deposit(acc, amt);
                    }
                    case 3 -> {
                        System.out.print("Account No: "); String acc = sc.nextLine();
                        System.out.print("Amount: "); double amt = Double.parseDouble(sc.nextLine());
                        bank.withdraw(acc, amt);
                    }
                    case 4 -> {
                        System.out.print("From: "); String f = sc.nextLine();
                        System.out.print("To: "); String t = sc.nextLine();
                        System.out.print("Amount: "); double amt = Double.parseDouble(sc.nextLine());
                        bank.transfer(f, t, amt);
                    }
                    case 5 -> {
                        System.out.print("Account No: "); String acc = sc.nextLine();
                        bank.getStatement(acc).forEach(System.out::println);
                    }
                    case 6 -> bank.listAll()
                            .forEach(a -> System.out.println(a.getAccountNumber() + " | " + a.getCustomer().getName() + " | " + a.getBalance()));
                    case 7 -> {
                        System.out.print("Name: "); String name = sc.nextLine();
                        bank.search(name)
                                .forEach(a -> System.out.println(a.getAccountNumber() + " | " + a.getCustomer().getName()));
                    }
                    case 8 -> { System.out.println("Goodbye!"); return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}