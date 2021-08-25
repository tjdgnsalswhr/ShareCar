package sharecar;

import sharecar.config.kafka.KafkaProcessor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_AcceptReservation(@Payload PaymentApproved paymentApproved){

        if(paymentApproved.validate()){
            System.out.println("##### Reservation PaymentApproved listener  : " + paymentApproved.toJson());
            Reservation reservation = new Reservation();
            reservation.setStatus("Reservation is Completed, orderId is : " + paymentApproved.getOrderId());
            reservation.setOrderId(paymentApproved.getOrderId());
            reservationRepository.save(reservation);
        }

        //System.out.println("\n\n##### listener AcceptReservation : " + paymentApproved.toJson() + "\n\n");



        // Sample Logic //
        // Reservation reservation = new Reservation();
        // reservationRepository.save(reservation);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_CancelReservation(@Payload OrderCancelled orderCancelled){

        if(orderCancelled.validate()){
            System.out.println("##### Reservation OrderCanceled listener  : " + orderCancelled.toJson());
            Reservation reserve = reservationRepository.findByOrderId(orderCancelled.getId());
	        //reserve.setStatus("Cancelled!");
            reservationRepository.delete(reserve);
        }

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
