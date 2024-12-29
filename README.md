Before launching, add database details to environment variables:
url: ${DB_URL}
username: ${DB_USER}
password: ${DB_PASSWORD}

Update 23-12-2024

1. DataInitializer updated (the project is now fully initialized on the first launch):
    - Initialize currencies if data is missing.
    - Populate the exchange_rates table if data is missing.
    - Add initial users if missing (admin, user).

2. UI updates:
    - Improved design (added Bootstrap, separated JS).
    - Logout functionality added.
    - Role-based authorization:
        - Admin panel appears only if the "Admin" role is passed during authorization.
    - Currency input fields replaced with dropdowns.
    - Currency dropdown list dynamically updates on changes.
    - Period input replaced with a toggle switch.

3. Bugfix:
    - Fixed issues with populating the Exchange Rates table on the frontend:
    - Since the requirements did not specify how the output should look, currency is currently displayed as a String.
    - Future plans include switching to Currency objects from String.

4. Added features:
    - WebSocket authorization via token.
    - Validation improvements:
    - setExchangeRate() (rate > 0, currency not empty).
    - getExchangeRate() (rate > 0, currencies not empty).
    - getMaxMinExchangeRate() (rate > 0, currency not empty).
    - Exchange request validation (amount > 0, currencies not empty).
    - Validation in SignUpRequest (NotNull, NotBlank).
    - Validation in SignInRequest (NotNull, NotBlank).
    - Validation in addCurrency() (NotNull, NotBlank).
    - Validation in deleteCurrency() (NotNull, NotBlank).
    - Validation in blockUser() (NotNull, NotBlank).
    - Validation in unblockUser() (NotNull, NotBlank).
    - Validation in setAdmin() (NotNull, NotBlank).
    - Validation in setUser() (NotNull, NotBlank).

5. Notes:
    - Some code on the frontend is written in jQuery. This was my first time using it, but if necessary, it can be replaced with plain JavaScript.

6. Question:
    - Should validation be implemented at the controller or service level? Should it be done using if statements or by throwing exceptions? I’d appreciate your comments!
