                                         Use Case Specification:- 

   Use Case 1: Book Ticket

a.	Primary Actor: Customer
b.	Preconditions: Customer is logged in and has selected a movie show timing and seats.
c.	Postconditions: Seats are reserved, payment is verified, and a booking confirmation is generated.
d.	Main Flow:
          1.	Customer reviews selected movie details, showtime, and chosen seats.
          2.	 System displays total price including taxes and booking fee.
          3.	 Customer clicks "Book Ticket" to proceed.
          4.	 System redirects Customer to the payment page Make Payment.
          5.	 Customer enters payment details and submits.
          6.	 System communicates with Payment Gateway to process transaction Process Payment.
          7.	 Payment Gateway verifies and approves transaction.
          8.	 System reserves the selected seats permanently for that show.
          9.	System generates ticket confirmation Generate Ticket.
          10.	 System displays confirmation screen with ticket details.




   Use Case 2: Add Movie

a.	Primary Actor: Admin
b.	Preconditions: Admin is authenticated and successfully logged into the Admin Dashboard.
c.	Postconditions: A new movie record is added to the system database and available for show scheduling.
d.	Main Flow:
           1.	 Admin selects "Add Movie" option from admin menu.
           2.	System displays the "Add Movie" form (fields for title, genre, duration, release date, description, poster image, etc.).
           3.	 Admin enters all required movie details and uploads poster artwork.
           4.	 Admin clicks "Submit".
           5.	System validates the inputs (ensures no required fields are blank).
           6.	 System saves movie details to the database.
           7.	 System displays success message: "Movie added successfully."



  Use Case 3: Cancel Booking

a.	Primary Actor: Customer
b.	Preconditions: Customer is logged in and has an active, upcoming booked ticket.
c.	Postconditions: Ticket is canceled, booked seats are freed up, and refund request is initiated.
d.	Main Flow:
           1.	 Customer opens "My Bookings" section and clicks "Cancel Booking" on an active ticket.
           2.	 System displays cancellation policy and refund amount breakdown.
           3.	 Customer confirms cancellation.
           4.	 System updates ticket status to "Canceled".
           5.	 System releases seats back to the available pool for that show.
           6.	 System triggers payment system to process refund Process Refund.
           7.	 System shows confirmation message with refund details and sends confirmation email.
