# CRC Cards — Movie Ticket Booking System

## User
| Class | Responsibilities | Collaborators |
|---|---|---|
| User | Store common user information; support common identity/login behavior | Customer, Admin |

## Customer
| Class | Responsibilities | Collaborators |
|---|---|---|
| Customer | Search movies; select shows/seats; create bookings; make payments; view/cancel bookings | Movie, Show, Seat, Booking, Payment, Coupon, Ticket |

## Admin
| Class | Responsibilities | Collaborators |
|---|---|---|
| Admin | Manage movies, theatres, screens, seats, shows and bookings | Movie, Theatre, Screen, Seat, Show, Booking |

## Movie
| Class | Responsibilities | Collaborators |
|---|---|---|
| Movie | Store movie information; provide movie details; identify movie for shows | Show, Admin, Customer |

## Theatre
| Class | Responsibilities | Collaborators |
|---|---|---|
| Theatre | Store theatre information; contain screens | Screen, Show, Admin |

## Screen
| Class | Responsibilities | Collaborators |
|---|---|---|
| Screen | Store screen information; contain seats; identify screen for a show | Theatre, Seat, Show |

## Seat
| Class | Responsibilities | Collaborators |
|---|---|---|
| Seat | Store seat information; maintain seat status; support seat selection | Screen, Show, Booking, Customer |

## Show
| Class | Responsibilities | Collaborators |
|---|---|---|
| Show | Store screening schedule; identify movie/screen/time; provide availability context | Movie, Screen, Seat, Booking, Customer, Admin |

## Booking
| Class | Responsibilities | Collaborators |
|---|---|---|
| Booking | Create booking; associate customer/show/seats; calculate amount; maintain status; support cancellation | Customer, Show, Seat, Payment, Ticket, Coupon |

## Payment
| Class | Responsibilities | Collaborators |
|---|---|---|
| Payment | Store payment details; maintain status; record transaction result; support refund status | Booking, Customer, Payment Gateway |

## Ticket
| Class | Responsibilities | Collaborators |
|---|---|---|
| Ticket | Store ticket details; represent confirmed booking; provide ticket information | Booking, Customer, Show, Movie |

## Coupon
| Class | Responsibilities | Collaborators |
|---|---|---|
| Coupon | Store coupon data; validate eligibility; calculate discount | Booking, Customer |
