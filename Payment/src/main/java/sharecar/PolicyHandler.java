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
    @Autowired 
    PaymentHistoryRepository paymentHistoryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_CancelPayment(@Payload ReservationCanceled reservationCanceled){

        if(reservationCanceled.validate()){

            System.out.println("\n\n##### listener CancelPayment : " + reservationCanceled.toJson() + "\n\n");
            PaymentHistory payment = paymentHistoryRepository.findByOrderId(reservationCanceled.getOrderId());
            payment.setStatus("PaymentCancelled!");
            paymentHistoryRepository.save(payment);
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
