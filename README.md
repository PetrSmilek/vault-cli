# Vault CLI – Správce hesel

Terminálová aplikace pro bezpečné ukládání hesel s **šifrováním AES-128**. Tento projekt se zaměřuje na kryptografii v Javě a používá architekturu DAO pattern.

---

## Installation & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/PetrSmilek/vault-cli.git
   cd vault-cli
   ```

2. **Set up encryption key (16 characters)**:
   ```bash
   echo "encryption.key=YOUR_16_CHAR_KEY" > src/main/resources/config.properties
   ```
   *(Example of a valid key: `7mKp9Qr2St4vW6yZ`)*

3. **Build and run the app**:
   ```bash
   mvn package
   java -jar target/vault-cli.jar
   ```

---

## Features

- Password encryption: AES-128 with random IV (Initialization Vector)  
- CRUD operations: Create / Read / Update / Delete password entries  
- Service overview: List of all stored services for the user  

---

## Tech Stack

| Component        | Technology                         |
|------------------|------------------------------------|
| Language         | Java 21                            |
| Encryption       | JCA (AES/CBC/PKCS5Padding)         |
| Database         | PostgreSQL 17                      |
| Testing          | JUnit 5, Mockito                   |
| Build tool       | Maven                              |

---

## Testing

```bash
mvn test
```

Projekt obsahuje unit testy pokrývající klíčové funkce šifrování a business logiky a integrační testy zaměřené na správnou interakci s databází.

---
