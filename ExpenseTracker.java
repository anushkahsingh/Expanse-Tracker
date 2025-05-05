import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

public class ExpenseTracker {
    private ArrayList<Expense> expenses;
    private Map<String, Double> budgets;
    private Scanner scanner;

    public ExpenseTracker() {
        expenses = new ArrayList<>();
        budgets = new HashMap<>();
        scanner = new Scanner(System.in);
    }

    // Add a new expense with description validation and budget check
    public void addExpense() {
        System.out.println("Enter amount:");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.println("Enter category (e.g., Food, Transport):");
        String category = scanner.nextLine();
        System.out.println("Enter date (YYYY-MM-DD):");
        LocalDate date = LocalDate.parse(scanner.nextLine());
        
        String description;
        while (true) {
            System.out.println("Enter description (letters only, no purely numeric input):");
            description = scanner.nextLine();
            // Check if description is not empty and not purely numeric
            if (!description.isEmpty() && !description.matches("\\d+")) {
                break;
            }
            System.out.println("Invalid input! Description must contain letters and cannot be purely numeric.");
        }

        // Check if adding this expense would equal or exceed the budget
        if (budgets.containsKey(category)) {
            double totalSpent = 0;
            for (Expense expense : expenses) {
                if (expense.getCategory().equalsIgnoreCase(category)) {
                    totalSpent += expense.getAmount();
                }
            }
            double budget = budgets.get(category);
            if (totalSpent + amount > budget) {
                System.out.println("Cannot add expense! Total spending (" + (totalSpent + amount) + ") would exceed budget (" + budget + "). Increase budget to add more expenses.");
                return;
            }
            if (totalSpent + amount == budget) {
                System.out.println("Cannot add expense! Total spending (" + (totalSpent + amount) + ") would exactly meet budget (" + budget + "). Increase budget to add more expenses.");
                return;
            }
        }

        Expense expense = new Expense(amount, category, date, description);
        expenses.add(expense);
        System.out.println("Expense added successfully! Current expense count: " + expenses.size());

        // Check budget
        checkBudget(category, amount);
    }

    // Edit an existing expense
    public void editExpense() {
        viewExpenses();
        System.out.println("Enter the index of the expense to edit (0 to cancel):");
        int index = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        if (index == 0 || index > expenses.size()) {
            System.out.println("Invalid index or cancelled.");
            return;
        }

        Expense expense = expenses.get(index - 1);
        System.out.println("Editing: " + expense);
        System.out.println("Enter new amount (or press Enter to keep " + expense.getAmount() + "):");
        String input = scanner.nextLine();
        if (!input.isEmpty()) {
            expense.setAmount(Double.parseDouble(input));
        }

        System.out.println("Enter new category (or press Enter to keep " + expense.getCategory() + "):");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            expense.setCategory(input);
        }

        System.out.println("Enter new date (YYYY-MM-DD) (or press Enter to keep " + expense.getDate() + "):");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            expense.setDate(LocalDate.parse(input));
        }

        System.out.println("Enter new description (or press Enter to keep " + expense.getDescription() + "):");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            expense.setDescription(input);
        }

        System.out.println("Expense updated successfully!");
    }

    // Delete an expense
    public void deleteExpense() {
        viewExpenses();
        System.out.println("Enter the index of the expense to delete (0 to cancel):");
        int index = scanner.nextInt();
        if (index == 0 || index > expenses.size()) {
            System.out.println("Invalid index or cancelled.");
            return;
        }

        expenses.remove(index - 1);
        System.out.println("Expense deleted successfully!");
    }

    // View all expenses
    public void viewExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded. Add expenses using option 1.");
            return;
        }
        System.out.println("Current Expenses (Total: " + expenses.size() + "):");
        for (int i = 0; i < expenses.size(); i++) {
            System.out.println((i + 1) + ". " + expenses.get(i));
        }
    }

    // Set budget for a category
    public void setBudget() {
        System.out.println("Enter category to set budget for:");
        String category = scanner.nextLine();
        System.out.println("Enter budget amount:");
        double budget = scanner.nextDouble();
        budgets.put(category, budget);
        System.out.println("Budget set for " + category + ": " + budget);
    }

    // Check if budget is exceeded or approaching
    private void checkBudget(String category, double newAmount) {
        if (!budgets.containsKey(category)) {
            return;
        }

        double totalSpent = 0;
        for (Expense expense : expenses) {
            if (expense.getCategory().equalsIgnoreCase(category)) {
                totalSpent += expense.getAmount();
            }
        }

        double budget = budgets.get(category);
        double remainingBalance = budget - totalSpent;

        if (totalSpent > budget) {
            System.out.println("Warning: Budget exceeded for " + category + "! Spent: " + totalSpent + ", Budget: " + budget + ", Remaining Balance: " + remainingBalance);
        } else if (totalSpent > budget * 0.9) {
            System.out.println("Alert: Approaching budget limit for " + category + "! Spent: " + totalSpent + ", Budget: " + budget + ", Remaining Balance: " + remainingBalance);
        }
    }

    // Summarize expenses by category
    public void summarizeExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to summarize.");
            return;
        }

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double amount = expense.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        System.out.println("Expense Summary by Category:");
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Save expenses to a file
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new File("expenses.txt"))) {
            for (Expense expense : expenses) {
                writer.println(expense.getAmount() + "," + expense.getCategory() + "," +
                              expense.getDate() + "," + expense.getDescription());
            }
            System.out.println("Expenses saved to file.");
        } catch (FileNotFoundException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    // Load expenses from a file
    public void loadFromFile() {
        try (Scanner fileScanner = new Scanner(new File("expenses.txt"))) {
            expenses.clear();
            while (fileScanner.hasNextLine()) {
                String[] parts = fileScanner.nextLine().split(",");
                double amount = Double.parseDouble(parts[0]);
                String category = parts[1];
                LocalDate date = LocalDate.parse(parts[2]);
                String description = parts[3];
                expenses.add(new Expense(amount, category, date, description));
            }
            System.out.println("Expenses loaded from file.");
        } catch (FileNotFoundException e) {
            System.out.println("No saved expenses found.");
        }
    }

    // Main menu
    public void run() {
        while (true) {
            System.out.println("\nExpense Tracker Menu:");
            System.out.println("1. Add Expense");
            System.out.println("2. Edit Expense");
            System.out.println("3. Delete Expense");
            System.out.println("4. View Expenses");
            System.out.println("5. Set Budget");
            System.out.println("6. Summarize Expenses");
            System.out.println("7. Save to File");
            System.out.println("8. Load from File");
            System.out.println("9. Exit");
            System.out.println("Enter choice:");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addExpense();
                    break;
                case 2:
                    editExpense();
                    break;
                case 3:
                    deleteExpense();
                    break;
                case 4:
                    viewExpenses();
                    break;
                case 5:
                    setBudget();
                    break;
                case 6:
                    summarizeExpenses();
                    break;
                case 7:
                    saveToFile();
                    break;
                case 8:
                    loadFromFile();
                    break;
                case 9:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void main(String[] args) {
        ExpenseTracker tracker = new ExpenseTracker();
        tracker.run();
    }
}