package com.vaultcli;

import com.vaultcli.model.User;
import com.vaultcli.service.AuthService;
import com.vaultcli.service.PasswordService;
import java.util.Scanner;

public class Main {
    private static final AuthService authService = new AuthService();
    private static final PasswordService passwordService = new PasswordService();
    private static User currentUser = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        showWelcomeScreen();

        while (true) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private static void showWelcomeScreen() {
        System.out.println("=======================================");
        System.out.println("VAULT-CLI");
        System.out.println("Bezpečný správce hesel v konzoli");
        System.out.println("=======================================");
    }

    private static void showAuthMenu() {
        System.out.println("\n1. Přihlásit se");
        System.out.println("2. Registrovat se");
        System.out.println("3. Ukončit aplikaci");
        System.out.print("Vyberte možnost (číslo): ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                System.out.println("Ukončuji aplikaci...");
                System.exit(0);
                break;
            default:
                System.out.println("Neplatná volba!");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n=== HLAVNÍ MENU ===");
        System.out.println("1. Zobrazit všechny služby");
        System.out.println("2. Přidat nové heslo");
        System.out.println("3. Zobrazit heslo");
        System.out.println("4. Aktualizovat heslo");
        System.out.println("5. Smazat heslo");
        System.out.println("6. Odhlásit se");
        System.out.print("Vyberte možnost: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                showAllPasswords();
                break;
            case 2:
                addPassword();
                break;
            case 3:
                showPassword();
                break;
            case 4:
                updatePassword();
                break;
            case 5:
                deletePassword();
                break;
            case 6:
                currentUser = null;
                System.out.println("Úspěšně odhlášeno.");
                break;
            default:
                System.out.println("Neplatná volba!");
        }
    }

    private static void login() {
        System.out.print("\nZadejte uživatelské jméno: ");
        String username = scanner.nextLine();
        System.out.print("Zadejte heslo: ");
        String password = scanner.nextLine();

        User user = authService.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Přihlášení úspěšné!");
        } else {
            System.out.println("Neplatné přihlašovací údaje!");
        }
    }

    private static void register() {
        System.out.print("\nZadejte nové uživatelské jméno: ");
        String username = scanner.nextLine();
        System.out.print("Zadejte heslo: ");
        String password = scanner.nextLine();

        if (authService.registerUser(username, password)) {
            System.out.println("Registrace úspěšná! Nyní se můžete přihlásit.");
        } else {
            System.out.println("Uživatel již existuje nebo registrace selhala!");
        }
    }

    private static void showAllPasswords() {
        System.out.println("\n=== Vaše služby a hesla ===");
        passwordService.getAllServiceNamesForUser(currentUser.getId())
                .forEach(service -> System.out.println("- " + service));
    }

    private static void addPassword() {
        System.out.print("\nZadejte název služby: ");
        String service = scanner.nextLine();
        System.out.print("Zadejte heslo: ");
        String password = scanner.nextLine();

        if (passwordService.addPassword(currentUser.getId(), service, password)) {
            System.out.println("Heslo úspěšně uloženo!");
        } else {
            System.out.println("Ukládání hesla selhalo!");
        }
    }

    private static void showPassword() {
        System.out.print("\nZadejte název služby: ");
        String service = scanner.nextLine();
        String password = passwordService.getPassword(currentUser.getId(), service);

        if (password != null) {
            System.out.println("Heslo pro " + service + ": " + password);
        } else {
            System.out.println("Heslo nebylo nalezeno!");
        }
    }

    private static void updatePassword() {
        System.out.print("\nZadejte název služby: ");
        String service = scanner.nextLine();
        System.out.print("Zadejte nové heslo: ");
        String newPassword = scanner.nextLine();

        if (passwordService.updatePassword(currentUser.getId(), service, newPassword)) {
            System.out.println("Heslo úspěšně aktualizováno!");
        } else {
            System.out.println("Aktualizace hesla selhala!");
        }
    }

    private static void deletePassword() {
        System.out.print("\nZadejte název služby: ");
        String service = scanner.nextLine();

        if (passwordService.deletePassword(currentUser.getId(), service)) {
            System.out.println("Heslo úspěšně smazáno!");
        } else {
            System.out.println("Mazání hesla selhalo!");
        }
    }
}