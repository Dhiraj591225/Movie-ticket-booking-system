# CineBook login and registration setup

This update adds Spring Security session authentication, BCrypt password hashing, customer registration, login/logout pages, and authenticated booking/payment operations.

## Before running

1. Add your MySQL password to the environment; do not commit it into `application.properties`.
   - Windows PowerShell: `$env:DB_PASSWORD="your-new-mysql-password"`
   - Optional: `$env:DB_USERNAME="root"`
   - Optional: `$env:DB_URL="jdbc:mysql://localhost:3306/movie_booking"`
2. The database password that was present in the uploaded project file should be changed in MySQL because it was included in a shared project archive.
3. Reload Maven dependencies in VS Code after adding Spring Security.
4. Start the application and open `http://localhost:8080/`.

## Demo account migration

When the application starts, the existing demo users' old plain-text passwords are converted to BCrypt hashes if needed. On a fresh database, the seeded demo accounts are:

- Customer: `demo@cinebook.com` / `password123`
- Admin: `admin@cinebook.com` / `admin123`

Change/remove these demo credentials before deploying anywhere public.

## Authentication endpoints

- `GET /auth/csrf` — initializes the CSRF cookie.
- `POST /auth/register` — JSON `{ "name": "Test User", "email": "test@example.com", "password": "password123" }`.
- `POST /auth/login` — JSON `{ "email": "test@example.com", "password": "password123" }`; the response sets a session cookie.
- `GET /auth/me` — returns the signed-in account.
- `POST /auth/logout` — invalidates the session.

For POST requests from Postman, first call `GET /auth/csrf`, keep cookies enabled, read the `XSRF-TOKEN` cookie, and send its value in the `X-XSRF-TOKEN` header. Keep the same cookie jar for login and subsequent requests.

## Booking changes

`POST /bookings/with-seats` now accepts only:

```json
{
  "showId": 1,
  "seatIds": [1, 2]
}
```

Do not send `customerId`. The backend gets the customer ID from the authenticated session. Booking history is now `GET /bookings/me/detailed`. The payment endpoint verifies that the booking belongs to the signed-in customer.

## Browser flow

A guest can browse movies and showtimes. Clicking **Book Tickets** redirects to login and carries the selected movie ID in the return URL. After login, CineBook reopens that movie's showtime selection. Registration returns the user to login; the user then signs in and continues.
