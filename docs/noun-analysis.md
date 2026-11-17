# Noun Analysis — Movie Ticket Booking System

## Candidate nouns
Customer, Admin, User, Movie, Theatre, Screen, Seat, Show, Booking, Payment, Ticket, Coupon, Payment Gateway, System Scheduler, Movie Details, Search Criteria, Cancellation Policy, Payment Confirmation, Booking Confirmation, Reminder, Refund, Genre, Rating, Duration, Date, Time.

## Filters applied
1. Remove duplicates/synonyms.
2. Remove nouns that are better represented as attributes.
3. Remove actions/processes and temporary request data.
4. Remove external actors/systems from the core domain model.
5. Remove UI messages/results.
6. Retain nouns with meaningful state, behavior, or relationships.

## Filtering results

| Candidate | Decision | Reason |
|---|---|---|
| Customer | Keep | Core user who books tickets |
| Admin | Keep | Manages the booking system |
| User | Keep | Common abstraction for Customer and Admin |
| Movie | Keep | Core domain object |
| Theatre | Keep | Venue for screenings |
| Screen | Keep | A theatre contains screens |
| Seat | Keep | Required for seat selection |
| Show | Keep | Scheduled movie screening |
| Booking | Keep | Represents a reservation |
| Payment | Keep | Represents payment state/details |
| Ticket | Keep | Issued after successful booking |
| Coupon | Keep | Optional discount object |
| Payment Gateway | Remove | External actor/system |
| System Scheduler | Remove | External/time-triggered actor |
| Movie Details | Remove | Attributes of Movie |
| Search Criteria | Remove | Input/request data |
| Cancellation Policy | Remove | Business rule in current scope |
| Payment Confirmation | Remove | Result/message |
| Booking Confirmation | Remove | Result/message |
| Reminder | Remove | Notification behavior, not core entity |
| Refund | Remove | Payment operation |
| Genre | Remove | Movie attribute |
| Rating | Remove | Movie attribute |
| Duration | Remove | Movie attribute |
| Date | Remove | Show/booking attribute |
| Time | Remove | Show/booking attribute |

## Survivors
`User`, `Customer`, `Admin`, `Movie`, `Theatre`, `Screen`, `Seat`, `Show`, `Booking`, `Payment`, `Ticket`, `Coupon`.

`Payment Gateway` and `System Scheduler` remain actors in the use-case model, not core domain classes.
