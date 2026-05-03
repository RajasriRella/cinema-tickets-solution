package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImplTest {

    private TicketPaymentService ticketpaymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setup() {
        ticketpaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketpaymentService, seatReservationService);
    }

    @Test
    void shouldPurchaseTicketsSuccessfully() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(888L, adult, child, infant);

        verify(ticketpaymentService).makePayment(888L, 130); 
        verify(seatReservationService).reserveSeat(888L, 6); // no seat for infant
    }

    @Test
    void shouldThrowExceptionWhenTicketRequestIsNull() {
        TicketTypeRequest request = null;

        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(888L, request);
        });

    }

    @Test
    void shouldThrowExceptionWhenTicketQuantityIsZero() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);

        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(888L, adult);
        });

    }

     @Test
    void shouldThrowExceptionWhenTicketQuantityIsNegative() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -2);

        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(888L, adult);
        });

    }

    @Test
    void shouldNotReserveSeatForInfantTickets() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(888L, adult, infant);
        verify(seatReservationService).reserveSeat(888L, 2); // infant not counted
    }

    @Test
    void shouldThrowExceptionWhenAccounIdIsInvalid() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);

        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(-1L, adult);
        });

    }

    @Test
    void shouldThrowExceptionWhenInfantTicketPurchasedWithoutAdultTicket() {
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(888L, infant);
        });
    }

    @Test
    void shouldThrowExceptionWhenChildTicketPurchasedWithoutAdultTicket() {
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
    
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(888L, child);
        });
    
    }

    @Test
    void shouldThrowExceptionWhenMoreThan25TicketsRequested() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);
        
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(888L, adult);
        });
    }
}
