package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int ADULT_PRICE = 25;
    private static final int CHILD_PRICE = 15;
    private static final int MAX_TICKETS = 25;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService){

        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);

        int adultTickets = 0;
        int childTickets = 0;
        int infantTickets = 0;

       //Count tickets for each type(adult, child, infant) 
       for (TicketTypeRequest request : ticketTypeRequests) {
            if(request == null) {
                throw new InvalidPurchaseException("Ticket request cannot be null");
            }
            
            if(request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException("Ticket quantity must be atleast 1");
            }
             switch (request.getTicketType()){
                case ADULT -> adultTickets += request.getNoOfTickets();
                case CHILD -> childTickets += request.getNoOfTickets();
                case INFANT -> infantTickets += request.getNoOfTickets();
            }
        } 
            
        int totalTickets = adultTickets + childTickets + infantTickets;

        validateMaximumTickets(totalTickets);
        validateAdultRequirement(adultTickets, childTickets, infantTickets);
        
        int totalAmount = adultTickets * ADULT_PRICE + childTickets * CHILD_PRICE;
        int totalSeats = adultTickets + childTickets; //infant do not require a seat

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    //Maximum 25 tickets allowed per transaction
    private void validateMaximumTickets(int totalTickets){
      if (totalTickets > MAX_TICKETS) {
             throw new InvalidPurchaseException("A maximum of 25 tickets can be purchased per transaction");

        }
    }

    //Child and Infant tickets requires atleast one adult
    private void validateAdultRequirement(int adultTickets, int childTickets, int infantTickets){
        if((childTickets > 0 || infantTickets > 0) && adultTickets == 0) {
             throw new InvalidPurchaseException("Child and infant tickets cannot be purchased without atleast one adult ticket");
        }
    }
    
    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0){
            throw new InvalidPurchaseException("Account ID must be greater than 0");
        }
    }

    private void validateTicketRequests(TicketTypeRequest...requests) {
        if(requests == null || requests.length == 0) {
            throw new InvalidPurchaseException("Atleast 1 ticket must be requested");
        }
    }
}
